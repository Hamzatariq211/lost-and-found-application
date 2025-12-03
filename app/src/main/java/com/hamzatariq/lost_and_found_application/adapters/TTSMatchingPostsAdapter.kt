package com.hamzatariq.lost_and_found_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.hamzatariq.lost_and_found_application.R
import com.hamzatariq.lost_and_found_application.models.MatchingPost

class TTSMatchingPostsAdapter(
    private val posts: List<MatchingPost>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<TTSMatchingPostsAdapter.ViewHolder>() {

    private var highlightedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemDescription: TextView = view.findViewById(R.id.itemDescription)
        val itemLocation: TextView = view.findViewById(R.id.itemLocation)
        val matchScore: TextView = view.findViewById(R.id.matchScore)
        val matchScoreValue: TextView = view.findViewById(R.id.matchScoreValue)
        val userName: TextView = view.findViewById(R.id.userName)
        val userContact: TextView = view.findViewById(R.id.userContact)
        val itemStatus: TextView = view.findViewById(R.id.itemStatus)
        val speakingIndicator: TextView = view.findViewById(R.id.speakingIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tts_matching_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        holder.itemName.text = post.item_name
        holder.itemDescription.text = post.item_description
        holder.itemLocation.text = "📍 ${post.location}"
        holder.matchScoreValue.text = "${post.match_score}%"
        holder.userName.text = "Posted by: ${post.user.full_name}"
        holder.userContact.text = "📞 ${post.user.mobile_number}"

        // Set status with color
        when (post.status) {
            "active" -> {
                holder.itemStatus.text = "✓ Active"
                holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_light))
            }
            "resolved" -> {
                holder.itemStatus.text = "✓ Returned to Owner"
                holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
            }
            else -> {
                holder.itemStatus.text = "Deleted"
                holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_light))
            }
        }

        // Highlight the currently speaking post
        if (position == highlightedPosition) {
            holder.cardView.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.button_purple)
            )
            holder.speakingIndicator.visibility = View.VISIBLE
            holder.cardView.elevation = 12f
        } else {
            holder.cardView.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.dark_card_purple)
            )
            holder.speakingIndicator.visibility = View.GONE
            holder.cardView.elevation = 4f
        }

        // Set match score color based on value
        val scoreColor = when {
            post.match_score >= 80 -> android.R.color.holo_green_light
            post.match_score >= 50 -> android.R.color.holo_orange_light
            else -> android.R.color.holo_red_light
        }
        holder.matchScoreValue.setTextColor(holder.itemView.context.getColor(scoreColor))

        // Click to speak individual post
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = posts.size

    fun setHighlightedPosition(position: Int) {
        val previousPosition = highlightedPosition
        highlightedPosition = position

        // Notify changes for smooth animation
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
}

