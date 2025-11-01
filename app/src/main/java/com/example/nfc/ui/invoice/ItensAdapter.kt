package com.example.nfc.ui.invoice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nfc.data.model.ItemNFCe
import com.example.nfc.databinding.ItemNfceBinding
import java.text.NumberFormat
import java.util.*

class ItensAdapter : ListAdapter<ItemNFCe, ItensAdapter.ItemViewHolder>(ItemDiffCallback()) {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemNfceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ItemViewHolder(
        private val binding: ItemNfceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ItemNFCe) {
            binding.apply {
                textDescricao.text = item.descricao
                textCodigo.text = "Cód: ${item.codigo}"
                textQuantidade.text = "${item.quantidade} ${item.unidade}"
                textValorUnitario.text = currencyFormat.format(item.valorUnitario)
                textValorTotal.text = currencyFormat.format(item.valorTotal)
                
                // Mostrar NCM se disponível
                item.ncm?.let {
                    textNcm.text = "NCM: $it"
                } ?: run {
                    textNcm.text = ""
                }
            }
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<ItemNFCe>() {
    override fun areItemsTheSame(oldItem: ItemNFCe, newItem: ItemNFCe): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: ItemNFCe, newItem: ItemNFCe): Boolean {
        return oldItem == newItem
    }
}
