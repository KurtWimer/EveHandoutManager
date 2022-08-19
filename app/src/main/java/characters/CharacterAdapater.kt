package characters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.R

class CharacterAdapater : RecyclerView.Adapter<CharacterAdapater.ViewHolder>(){

    private var characters : List<Character> = listOf<Character>(Character(1)) //TODO USE ESI Call to edit list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_character, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return characters.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.characterIcon.setImageResource(holder.characterIcon) TODO apply character icon
        holder.characterName.text = characters[position].name

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var characterIcon : ImageView
        var characterName : TextView
        var logoutButton : Button

        init {
            characterIcon = itemView.findViewById(R.id.character_icon)
            characterName = itemView.findViewById(R.id.character_name)
            logoutButton = itemView.findViewById(R.id.logout_button)
        }
    }

}