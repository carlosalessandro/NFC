package com.example.nfc

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nfc.data.database.NFCeDatabase
import com.example.nfc.data.repository.NFCeRepository
import com.example.nfc.databinding.ActivityMainBinding
import com.example.nfc.utils.SampleDataGenerator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "onCreate: Iniciando aplicação")
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d("MainActivity", "onCreate: Layout definido")
        
        setupNavigation()
        
        Log.d("MainActivity", "onCreate: Navegação configurada")
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Configurar bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configurar action bar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.scannerFragment,
                R.id.invoiceListFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_sample_data -> {
                addSampleData()
                true
            }
            R.id.action_clear_data -> {
                clearAllData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun addSampleData() {
        lifecycleScope.launch {
            try {
                val database = NFCeDatabase.getDatabase(this@MainActivity)
                val repository = NFCeRepository(database.nfceDao())
                
                val sampleData = SampleDataGenerator.generateSampleNFCe(5)
                
                sampleData.forEach { (nfce, itens) ->
                    repository.insertNFCeWithItens(nfce, itens)
                }
                
                Toast.makeText(
                    this@MainActivity,
                    "5 notas de exemplo adicionadas com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao adicionar dados: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun clearAllData() {
        lifecycleScope.launch {
            try {
                val database = NFCeDatabase.getDatabase(this@MainActivity)
                database.clearAllTables()
                
                Toast.makeText(
                    this@MainActivity,
                    "Todos os dados foram removidos!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao limpar dados: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
