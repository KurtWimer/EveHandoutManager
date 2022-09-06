package accounts

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.databinding.FragmentAccountBinding
import okhttp3.internal.toImmutableList

class AccountFragment : Fragment() {
    private var adapter = AccountAdapter() //TODO dagger instantiation]
    private var _binding : FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel : AccountViewModel by activityViewModels()
    private val args : AccountFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding  = DataBindingUtil.inflate(
            inflater, R.layout.fragment_account, container, false)
        //bind xml data to viewModel
        binding.viewModel = accountViewModel

        //Set Up Live Data Observers
        accountViewModel.accountList.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("AccountList", it.toString())
                adapter.submitList(it.toImmutableList())
            }
        })
        binding.accountList.adapter = adapter

        accountViewModel.navigateToSSO.observe(viewLifecycleOwner) { intent ->
            if (intent != null) {
                Log.i("CharacterManager", "Attempting to open browser for eve SSO login")
                //open a web browser if there is one, I really hope they have a web browser
                try {
                    requireNotNull(this.activity).startActivity(intent)
                } catch (exception: ActivityNotFoundException) {
                    Log.e("CharacterManager", exception.message!!)
                    //TODO warn user they need a web browser
                }
                accountViewModel.onLoginButtonComplete()
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