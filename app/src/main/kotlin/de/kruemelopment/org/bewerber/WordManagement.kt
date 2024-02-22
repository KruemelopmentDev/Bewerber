package de.kruemelopment.org.bewerber

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WordManagement : AppCompatActivity(), AdapterChanges {
    private var type: String? = null
    private var dataBaseHelper: DataBaseHelper? = null
    private val items: MutableList<String> = ArrayList()
    private var wordManagementAdapter: WordManagementAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.adapter)
        type = intent.getStringExtra("type")
        dataBaseHelper = DataBaseHelper(this)
        val res = dataBaseHelper!!.getAllData(type)
        if (res.count > 0) {
            while (res.moveToNext()) {
                items.add(res.getString(1))
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (type == "worter") supportActionBar!!.title =
            "gespeicherte Wörter" else if (type == "erganzung") supportActionBar!!.title =
            "eingegebene Wörter"
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        wordManagementAdapter = WordManagementAdapter(items, dataBaseHelper!!, type, this)
        recyclerView.setHasFixedSize(false)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.recycleChildrenOnDetach = true
        recyclerView.layoutManager = linearLayoutManager
        val dividerItemDecoration = CustomDivider(recyclerView.context)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.adapter = wordManagementAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    override fun adapterIsEmpty() {
        items.clear()
        if (type == "worter") {
            items.addAll(mutableListOf("Cola", "Käse", "Krümelopment Dev"))
            dataBaseHelper!!.insertData("Cola", "worter")
            dataBaseHelper!!.insertData("Käse", "worter")
            dataBaseHelper!!.insertData("Krümelopment Dev", "worter")
            wordManagementAdapter!!.notifyItemRangeInserted(0, 3)
        } else if (type == "erganzung") {
            dataBaseHelper!!.insertData("Cola", "erganzung")
            dataBaseHelper!!.insertData("Käse", "erganzung")
            dataBaseHelper!!.insertData("Bier", "erganzung")
            dataBaseHelper!!.insertData("Salami", "erganzung")
            dataBaseHelper!!.insertData("Fanta", "erganzung")
            dataBaseHelper!!.insertData("Sprite", "erganzung")
            dataBaseHelper!!.insertData("Wasser", "erganzung")
            dataBaseHelper!!.insertData("Döner", "erganzung")
            dataBaseHelper!!.insertData("Rosen", "erganzung")
            items.addAll(
                mutableListOf(
                    "Cola",
                    "Käse",
                    "Bier",
                    "Salami",
                    "Fanta",
                    "Sprite",
                    "Wasser",
                    "Döner",
                    "Rosen"
                )
            )
            wordManagementAdapter!!.notifyItemRangeInserted(0, 9)
        }
    }
}

interface AdapterChanges {
    fun adapterIsEmpty()
}