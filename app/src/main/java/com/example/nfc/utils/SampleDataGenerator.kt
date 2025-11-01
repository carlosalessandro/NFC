package com.example.nfc.utils

import com.example.nfc.data.model.ItemNFCe
import com.example.nfc.data.model.NFCe
import kotlin.random.Random

object SampleDataGenerator {
    
    private val empresas = listOf(
        "Supermercado ABC Ltda" to "12345678000195",
        "Farmácia Central" to "98765432000187",
        "Posto Shell" to "11223344000156",
        "McDonald's" to "55667788000123",
        "Magazine Luiza" to "99887766000145"
    )
    
    private val produtos = listOf(
        "Arroz Branco 5kg" to 25.90,
        "Feijão Preto 1kg" to 8.50,
        "Açúcar Cristal 1kg" to 4.20,
        "Óleo de Soja 900ml" to 6.80,
        "Leite Integral 1L" to 4.50,
        "Pão de Forma" to 5.20,
        "Refrigerante Cola 2L" to 7.90,
        "Sabonete Dove" to 3.40,
        "Shampoo 400ml" to 12.90,
        "Pasta de Dente" to 8.70
    )
    
    fun generateSampleNFCe(count: Int = 10): List<Pair<NFCe, List<ItemNFCe>>> {
        val samples = mutableListOf<Pair<NFCe, List<ItemNFCe>>>()
        
        repeat(count) {
            val empresa = empresas.random()
            val dataEmissao = generateRandomTimestamp()
            val chaveAcesso = generateChaveAcesso()
            val numeroNota = Random.nextInt(1000, 999999).toString()
            val serie = Random.nextInt(1, 999).toString().padStart(3, '0')
            
            // Gerar itens
            val numItens = Random.nextInt(1, 6)
            val itens = mutableListOf<ItemNFCe>()
            var valorTotalNota = 0.0
            
            repeat(numItens) { index ->
                val produto = produtos.random()
                val quantidade = Random.nextDouble(1.0, 5.0)
                val valorUnitario = produto.second + Random.nextDouble(-2.0, 5.0)
                val valorTotal = quantidade * valorUnitario
                valorTotalNota += valorTotal
                
                itens.add(
                    ItemNFCe(
                        nfceId = 0, // Será definido após inserir a NFC-e
                        codigo = Random.nextInt(100000, 999999).toString(),
                        descricao = produto.first,
                        quantidade = quantidade,
                        unidade = "UN",
                        valorUnitario = valorUnitario,
                        valorTotal = valorTotal,
                        ncm = generateNCM(),
                        cfop = "5102"
                    )
                )
            }
            
            val nfce = NFCe(
                chaveAcesso = chaveAcesso,
                numeroNota = numeroNota,
                serie = serie,
                dataEmissao = dataEmissao,
                cnpjEmitente = empresa.second,
                nomeEmitente = empresa.first,
                valorTotal = valorTotalNota,
                valorTributos = valorTotalNota * 0.15, // 15% de tributos aproximadamente
                status = listOf("ATIVA", "ATIVA", "ATIVA", "CANCELADA").random() // 75% ativas
            )
            
            samples.add(nfce to itens)
        }
        
        return samples
    }
    
    private fun generateRandomTimestamp(): Long {
        val now = System.currentTimeMillis()
        val sixMonthsAgo = now - (6L * 30L * 24L * 60L * 60L * 1000L) // 6 meses atrás
        return Random.nextLong(sixMonthsAgo, now)
    }
    
    private fun generateChaveAcesso(): String {
        // Gera uma chave de acesso válida de 44 dígitos
        val uf = "41" // Paraná
        val anoMes = "2411" // Novembro 2024
        val cnpj = "12345678000195"
        val modelo = "65" // NFC-e
        val serie = Random.nextInt(1, 999).toString().padStart(3, '0')
        val numero = Random.nextInt(1, 999999).toString().padStart(9, '0')
        val tipoEmissao = "1"
        val codigoNumerico = Random.nextInt(10000000, 99999999).toString()
        val dv = Random.nextInt(0, 9).toString()
        
        return uf + anoMes + cnpj + modelo + serie + numero + tipoEmissao + codigoNumerico + dv
    }
    
    private fun generateNCM(): String {
        // Gera um NCM válido de 8 dígitos
        val ncmPrefixes = listOf("0401", "1701", "2106", "3304", "8471")
        val prefix = ncmPrefixes.random()
        val suffix = Random.nextInt(1000, 9999).toString()
        return prefix + suffix
    }
}
