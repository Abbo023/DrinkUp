package com.example.provaprogetto.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.provaprogetto.MainActivity
import com.example.provaprogetto.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class DrinkListFragment : Fragment() {

    private lateinit var navController: NavController
    private var mainActionBar: ActionBar? = null
    private lateinit var mainBottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActionBar = (activity as? AppCompatActivity)?.supportActionBar
        mainActionBar?.hide()
        mainBottomNavigationView = (activity as MainActivity).getMainBottomNavigationView()

        return inflater.inflate(R.layout.fragment_nav_drink_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                val navHostFragment = childFragmentManager.findFragmentById(R.id.pred_nav_host_fragment) as? NavHostFragment
                    ?: run {
                        val newNavHostFragment = NavHostFragment.create(R.navigation.nav_pred)
                        childFragmentManager.beginTransaction()
                            .replace(R.id.pred_nav_host_fragment, newNavHostFragment)
                            .setPrimaryNavigationFragment(newNavHostFragment)
                            .commitNow()
                        newNavHostFragment
                    }

                navController = navHostFragment.navController
                val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.pred_navigation)


                bottomNavigationView.setupWithNavController(navController)

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    if (destination.id == R.id.recipeDetailFragment1 ||
                        destination.id == R.id.drinkRecipeFragment1 ||
                        destination.id == R.id.addPersonalFragment ||
                        destination.id == R.id.personalDetailFragment) {
                        mainBottomNavigationView.visibility = View.GONE
                        bottomNavigationView.visibility = View.GONE
                    } else {
                        mainBottomNavigationView.visibility = View.VISIBLE
                        bottomNavigationView.visibility = View.VISIBLE

                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActionBar?.show()
    }

}
