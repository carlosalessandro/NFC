package com.example.nfc.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nfc.data.model.NFCe
import com.example.nfc.databinding.ItemRecentNfceBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RecentNFCeAdapter(
    private val onItemClick: (NFCe) -> Unit
) : ListAdapter<NFCe, RecentNFCeAdapter.NFCeViewHolder>(NFCeDiffCallback()) {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NFCeViewHolder {
        val binding = ItemRecentNfceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NFCeViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NFCeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class NFCeViewHolder(
        private val binding: ItemRecentNfceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(nfce: NFCe) {
            binding.apply {
                textNomeEmitente.text = nfce.nomeEmitente
                textNumeroNota.text = "Nota: ${nfce.numeroNota}"
                textDataEmissao.text = dateFormat.format(nfce.dataEmissao)
                textValorTotal.text = currencyFormat.format(nfce.valorTotal)
                
                // Status da nota
                textStatus.text = nfce.status
                when (nfce.status) {
                    "ATIVA" -> {
                        textStatus.setTextColor(
                            itemView.context.resources.getColor(android.R.color.holo_green_dark, null)
                        )
                    }
                    "CANCELADA" -> {
                        textStatus.setTextColor(
                            itemView.context.resources.getColor(android.R.color.holo_red_dark, null)
                        )
                    }
                    "PENDENTE" -> {
                        textStatus.setTextColor(
                            itemView.context.resources.getColor(android.R.color.holo_orange_dark, null)
                        )
                    }
                    else -> {
                        textStatus.setTextColor(
                            itemView.context.resources.getColor(android.R.color.darker_gray, null)
                        )
                    }
                }
            }
        }
    }
}

class NFCeDiffCallback : DiffUtil.ItemCallback<NFCe>() {
    override fun areItemsTheSame(oldItem: NFCe, newItem: NFCe): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: NFCe, newItem: NFCe): Boolean {
        return oldItem == newItem
    }
}
