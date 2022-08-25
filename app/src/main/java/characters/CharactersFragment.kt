package characters

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.databinding.FragmentCharactersBinding
import network.ESIRepo

class CharactersFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<CharacterAdapter.ViewHolder>? = null //TODO dagger instantiation]
    private var _binding : FragmentCharactersBinding? = null
    private val binding get() = _binding!!
    //lateinit var viewModel : CharactersViewModel //TODO dagger? factoryies viewModel instantialtion
    private val args : CharactersFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding  = DataBindingUtil.inflate(
            inflater, R.layout.fragment_characters, container, false)
        val characterViewModel = ViewModelProvider(this)[CharactersViewModel::class.java]
        //bind xml data to viewModel
        binding.viewModel = characterViewModel
        binding.characterList.adapter = adapter

        //Set Up Live Data Observers
        characterViewModel.navigateToSSO.observe(viewLifecycleOwner) { intent ->
            if (intent != null) {
                Log.i("CharacterManager", "Attempting to open browser for eve SSO login")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (args.code != null) {
            binding.viewModel?.handleCallback(requireNotNull(args.code))
        }
    }
}