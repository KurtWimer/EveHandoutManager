package characters

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.databinding.FragmentCharactersBinding
import network.ESIRepo

class CharactersFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<CharacterAdapter.ViewHolder>? = null
    //lateinit var viewModel : CharactersViewModel //TODO dagger? factoryies viewModel instantialtion
    lateinit var adapater: CharacterAdapter //TODO dagger instantiation
    private val repo = ESIRepo() //TODO dagger instantiation does this survive navigation?


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding : FragmentCharactersBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_characters, container, false)
        val characterViewModel = ViewModelProvider(this).get(CharactersViewModel::class.java)

        //bind xml data to viewModel
        binding.viewModel = characterViewModel
        binding.characterList.adapter = adapter

        //Set Up Live Data Observers
        characterViewModel.navigateToSSO.observe(viewLifecycleOwner) {
            if (it == true) {
                Log.i("CharacterManager", "Attempting to open browser for eve SSO login")
                val intent = repo.getLoginIntent(
                    getString(R.string.client_id),
                    getString(R.string.redirect_uri)
                )

                //open a web browser if there is one, I really hope they have a web browser
                try {
                    requireNotNull(this.activity).startActivity(intent)
                } catch (exception: ActivityNotFoundException) {
                    Log.e("CharacterManager", exception.message!!)
                    //TODO warn user they need a web browser
                }
                characterViewModel.onLoginButtonComplete()
            }
        }


        //TODO datasource
        return binding.root
    }
}