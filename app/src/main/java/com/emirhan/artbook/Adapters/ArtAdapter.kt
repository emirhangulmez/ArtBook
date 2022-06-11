package com.emirhan.artbook.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emirhan.artbook.Activities.DetailActivity
import com.emirhan.artbook.Models.Art
import com.emirhan.artbook.Models.ArtData
import com.emirhan.artbook.databinding.RecyclerRowBinding

class ArtAdapter(var artArray: ArrayList<Art>): RecyclerView.Adapter<ArtAdapter.BindingHolder>() {
    private lateinit var binding:RecyclerRowBinding
    class BindingHolder(binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        var text = binding.name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BindingHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        holder.text.text = artArray[position].name

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, DetailActivity::class.java).apply {
                putExtra("info",0)
            }
            ArtData.data = artArray[position].name
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artArray.size
    }

}