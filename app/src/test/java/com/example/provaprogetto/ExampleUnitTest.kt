package com.example.provaprogetto

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.ui.pred.PredFragmentViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PredFragmentViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var viewModel: PredFragmentViewModel

    @Mock
    private lateinit var repository: Repository

    @Mock
    private lateinit var drinksObserver: Observer<List<Drink>>

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var userRef: DocumentReference

    @Before
    fun setup() {
        val mockFirebaseApp = mock(FirebaseApp::class.java)

        mockStatic(FirebaseApp::class.java).use {
            `when`(FirebaseApp.initializeApp(any())).thenReturn(mockFirebaseApp)
            `when`(FirebaseApp.getInstance()).thenReturn(mockFirebaseApp)
            `when`(FirebaseApp.getInstance().get(FirebaseApp::class.java)).thenReturn(mockFirebaseApp)

            `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
            `when`(firebaseUser.uid).thenReturn("testUserId")

            val mockCollectionRef = mock(CollectionReference::class.java)
            `when`(firestore.collection("users")).thenReturn(mockCollectionRef)
            `when`(mockCollectionRef.document("testUserId")).thenReturn(userRef)

            repository = mock(Repository::class.java)

            viewModel = PredFragmentViewModel(application = mock())

            viewModel.drinks.observeForever(drinksObserver)
        }
    }


    @Test
    fun testFetchDrinkList() = runBlocking {
        val mockDrinkList = listOf(Drink(id = 1, name = "Mojito"))

        `when`(repository.getDrinkList()).thenReturn(MutableLiveData(mockDrinkList))

        viewModel.fetchDrinkList()

        verify(drinksObserver).onChanged(mockDrinkList)
    }

    @Test
    fun testToggleFavorite_addsToFavorite(): Unit = runBlocking {
        val drink = Drink(id = 1, name = "Mojito", isLike = false)
        val ricetteFav = mapOf("origin" to "pred", "drinkId" to drink.id.toString())

        `when`(userRef.get()).thenReturn(mock())
        `when`(userRef.update("ricette preferite", listOf(ricetteFav))).thenReturn(mock())

        viewModel.toggleFavorite(drink)

        verify(userRef).update("ricette preferite", listOf(ricetteFav))
    }

    @Test
    fun testToggleFavorite_removesFromFavorite(): Unit = runBlocking {
        val drink = Drink(id = 1, name = "Mojito", isLike = true)

        `when`(userRef.get()).thenReturn(mock())
        `when`(userRef.update("ricette preferite", emptyList<Map<String, String>>())).thenReturn(mock())

        viewModel.toggleFavorite(drink)

        verify(userRef).update("ricette preferite", emptyList<Map<String, String>>())
    }

    @Test
    fun testSearchDrinksByIngredientsAndName() = runBlocking {
        val ingredients = listOf("Rum", "Mint")
        val drinkName = "Mojito"
        val mockDrinkList = listOf(Drink(id = 1, name = "Mojito"))

        `when`(repository.searchDrinkByIngredient("Rum")).thenReturn(MutableLiveData(mockDrinkList))
        `when`(repository.searchDrinkByIngredient("Mint")).thenReturn(MutableLiveData(mockDrinkList))

        viewModel.searchDrinksByIngredientsAndName(ingredients, drinkName)

        verify(drinksObserver).onChanged(mockDrinkList)
    }

    @Test
    fun testRestoreFilterState() = runBlocking {
        val ingredients = listOf("Rum", "Mint")
        val drinkName = "Mojito"
        val mockDrinkList = listOf(Drink(id = 1, name = "Mojito"))

        viewModel.currentIngredients = ingredients
        viewModel.currentDrinkName = drinkName

        `when`(repository.searchDrinkByIngredient("Rum")).thenReturn(MutableLiveData(mockDrinkList))
        `when`(repository.searchDrinkByIngredient("Mint")).thenReturn(MutableLiveData(mockDrinkList))

        viewModel.restoreFilterState()

        verify(drinksObserver).onChanged(mockDrinkList)
    }
}
