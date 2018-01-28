package com.sauray.boursorama_test.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sauray.boursorama.model.Movement
import com.sauray.boursorama_test.R
import com.squareup.picasso.Picasso

/**
 * Created by Antoine Sauray on 28/01/2018.
 */
class MovementAdapter : RecyclerView.Adapter<MovementAdapter.MovementViewHolder>() {


    val dataset = ArrayList<Movement>()

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun add(movement: Movement) {
        dataset.add(movement)
        notifyItemInserted(dataset.size-1)
    }

    override fun onBindViewHolder(holder: MovementViewHolder?, position: Int) {
        holder?. let {
            val movement = dataset[position]
            Picasso.with(holder.view.context).load(R.drawable.ic_compare_arrows_black_24dp).into(holder.image)
            holder.amount.text = movement.amount
            holder.date.text = movement.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MovementViewHolder {
        parent?. let {
            val layoutInflater = LayoutInflater.from(it.context)
            return MovementViewHolder(layoutInflater.inflate(R.layout.view_movement, it, false))

        } ?: throw IllegalStateException("A view holder needs a parent to be created")
    }


    class MovementViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.image)
        val amount = view.findViewById<TextView>(R.id.amount)
        val date = view.findViewById<TextView>(R.id.date)
    }
}