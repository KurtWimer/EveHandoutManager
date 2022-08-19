package characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.databinding.FragmentCharactersBinding

class CharactersFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<CharacterAdapater.ViewHolder>? = null
    lateinit var viewModel : CharactersViewModel //TODO dagger? factoryies viewModel instantialtion
    lateinit var adapater: CharacterAdapater //TODO dagger instantiation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding : FragmentCharactersBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_characters, container, false)

        val application = requireNotNull(this.activity).application

        binding.characterList.adapter = adapter
        //TODO datasource
        return binding.root
    }
}