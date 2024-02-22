package de.kruemelopment.org.bewerber

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Infos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.infos)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val sloganCountTextView = findViewById<TextView>(R.id.textView9)
        val length = resources.getStringArray(R.array.sprueche).size
        val text = "Sprüche: $length"
        sloganCountTextView.text = text
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val count = resources.getStringArray(R.array.sprueche)
        for (index in count.indices) {
            count[index] = count[index].replace("_".toRegex(), "(eingegebenes Wort)")
            count[index] = count[index].replace("#".toRegex(), "(zufälliges Wort)")
            count[index] = count[index].replace("~".toRegex(), "(zufälliges Wort)")
        }
        val version = "App-Version: 4.2"
        (findViewById<View>(R.id.textView8) as TextView).text = version
        val adaptr = InfosListAdapter(count)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.recycleChildrenOnDetach = true
        recyclerView.layoutManager = linearLayoutManager
        val dividerItemDecoration = CustomDivider(recyclerView.context)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.adapter = adaptr
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
}
