package org.apache.openjpa.kernel.jpql;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.QueryContext;
import org.apache.openjpa.kernel.exps.Resolver;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class TestPopulate {

    private JPQLExpressionBuilder.ParsedJPQL parsedJPQL;
    private ExpressionStoreQuery mockExpressionStoreQuery;
    private QueryContext mockQueryContext;

    @Before
    public void setUp() {
        parsedJPQL = new JPQLExpressionBuilder.ParsedJPQL("SELECT u FROM User u");
        mockExpressionStoreQuery = mock(ExpressionStoreQuery.class);
        mockQueryContext = mock(QueryContext.class);
    }

    //TC1
    @Test
    public void nullInput(){
        Assert.assertThrows(
                "Ci si aspetta un'eccezione",
                NullPointerException.class,
                () -> parsedJPQL.populate(null)
        );
    }

    //TC2
    @Test
    public void queryContextNull(){
        // mocko l'ExpressionStoreQuery in modo che quando prova a recuperare il query context torna null
        when(mockExpressionStoreQuery.getContext()).thenReturn(null);

        Assert.assertThrows(
                "Ci si aspetta un'eccezione",
                NullPointerException.class,
                () -> parsedJPQL.populate(mockExpressionStoreQuery)
        );
    }

    //TC3
    @Test
    public void classAlreadySetInQueryContext(){
        // dico al mock dell'expressionStoreQuery che quando viene chiamato il context torna il relativo mock
        when(mockExpressionStoreQuery.getContext()).thenReturn(mockQueryContext);
        // per simulare che la classe sia giá settata al mockQueryContext faccio tornare un generico oggetto
        doReturn(Object.class).when(mockQueryContext).getCandidateType();
        // chiamo il vero metodo populate
        parsedJPQL.populate(mockExpressionStoreQuery);
        // verifico che venga chiamato il metodo per ottenere la classe impostata nel Query context
        verify(mockQueryContext).getCandidateType();
        // verifico che non avvengano piu interazioni con il QueryContext per avere la conferma che lo stato non cambi
        verifyNoMoreInteractions(mockQueryContext);

    }

    //TC4
    @Test
    public void cacheUsed() throws NoSuchFieldException, IllegalAccessException {
        // siccome il metodo populate accede direttamente all'attributo _candidateType non é
        // possibile usare i mock per cambiare il comportamento della getCandidateType ma siamo
        // costretti a usare la reflection per cambiare l´attributo in se
        Field cacheField = JPQLExpressionBuilder.ParsedJPQL.class.getDeclaredField("_candidateType");
        cacheField.setAccessible(true);
        cacheField.set(parsedJPQL, String.class);
        // dico al mock dell'expressionStoreQuery che quando viene chiamato il context torna il relativo mock
        when(mockExpressionStoreQuery.getContext()).thenReturn(mockQueryContext);
        // con il mock simulo che nel QueryContext non sia presente la classe
        when(mockQueryContext.getCandidateType()).thenReturn(null);
        // chiamo il vero metodo populate
        parsedJPQL.populate(mockExpressionStoreQuery);
        // verifichiamo che sia stato chiamato il metodo per impostare la classe candidata con quella che avevamo in cache
        verify(mockQueryContext, times(1)).setCandidateType(String.class, true);

        /**
        * Se il test passa possiamo escludere a priori che non sia stata utilizzata la cache.
         * Avendo mockato l'expressionStoreQuery la chiamata al costruttore di JPQLExpressionBuilder
         * Andrebbe in errore trovandosi con un oggetto vuoto
        */
    }


    //TC5
    @Test
    public void testP() {
        /*
         * SETUP PRELIMINARE
         * Assumiamo che parsedJPQL sia stato inizializzato nel @Before
         * con una query del tipo: "SELECT u FROM User u"
         */

        // 1. SETUP DELL'ECOSISTEMA (Mocking dei confini esterni)
        Resolver mockResolver = mock(Resolver.class);
        OpenJPAConfiguration mockConfig = mock(OpenJPAConfiguration.class);
        MetaDataRepository mockRepo = mock(MetaDataRepository.class);
        ClassMetaData mockClassMetaData = mock(ClassMetaData.class);

        // Cablaggio della catena di deleghe architetturali di OpenJPA
        when(mockExpressionStoreQuery.getResolver()).thenReturn(mockResolver);
        when(mockResolver.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getMetaDataRepositoryInstance()).thenReturn(mockRepo);

        /*
         * STRICT STUBBING:
         * Istruiamo il finto repository a rispondere SOLO ed ESCLUSIVAMENTE se
         * il JPQLExpressionBuilder gli chiede l'entità "User" con assertValid a false.
         * Se l'AST è rotto e cerca un'altra stringa, questo mock restituirà null
         * e farà fallire il test in modo inequivocabile.
         */
        when(mockRepo.getMetaData(eq("User"), any(ClassLoader.class), eq(false)))
                .thenReturn(mockClassMetaData);

        // Il fascicolo dei metadati dichiara di essere la classe String (la nostra entità finta)
        doReturn(String.class).when(mockClassMetaData).getDescribedType();

        // Configurazione del QueryContext per simulare una cache inizialmente vuota
        when(mockExpressionStoreQuery.getContext()).thenReturn(mockQueryContext);
        when(mockQueryContext.getCandidateType()).thenReturn(null);

        // 2. ESECUZIONE
        // Esecuzione genuina e inalterata del System Under Test.
        // Qui avverrà l'istanziazione reale di JPQLExpressionBuilder che
        // navigherà l'AST e interrogherà la nostra catena di mock.
        parsedJPQL.populate(mockExpressionStoreQuery);

        // 3. VERIFICA COMPORTAMENTALE

        // A. Verifichiamo che il metodo abbia letto lo stato iniziale
        verify(mockQueryContext).getCandidateType();

        // B. Verifichiamo che la classe trovata tramite l'AST (String.class)
        // sia stata passata al contesto per l'aggiornamento
        verify(mockQueryContext, times(1)).setCandidateType(String.class, true);

        // C. La Prova del Nove: certifichiamo che il framework interno abbia
        // effettivamente estratto la stringa "User" dall'albero sintattico
        // per interrogare il dizionario di OpenJPA
        verify(mockRepo, times(1)).getMetaData(eq("User"), any(ClassLoader.class), eq(false));

        // D. Sigilliamo il mock del contesto per evitare side-effect non previsti
        verifyNoMoreInteractions(mockQueryContext);
    }

}
