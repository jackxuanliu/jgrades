package website.jackl.jgrades.activity

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.SearchView
import android.view.View
import android.widget.TextView
import android.support.v7.widget.Toolbar
import android.util.Log
import website.jackl.jgrades.R
import website.jackl.jgrades.Data.District
import website.jackl.jgrades.recyclerAdapter.DistrictsAdapter
import website.jackl.jgrades.newStore
import website.jackl.jgrades.view.MyList

class DistrictSelectionActivity : GradesActivity<ConstraintLayout>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_district_selection)


        parent = findViewById(R.id.districtSelection_parent)
        toolbar = findViewById(R.id.districtSelection_toolbar)
        searchView = findViewById(R.id.districtSelection_searchView)
        title = findViewById(R.id.districtSelection_title)

        districts = findViewById(R.id.districtSelection_districts)
        adapter = DistrictsAdapter()
        districts.setAdapter(adapter)
        districts.onSrlPull = this::onServiceReady

        adapter.onItemClick = this::onDistrictClick
        adapter.onMissingDistrict = {
            val intent = Intent(this@DistrictSelectionActivity, ManualEntryActivity::class.java)
            startActivity(intent)
        }

        searchView.setOnSearchClickListener { onStartSearch() }
        searchView.setOnCloseListener { onEndSearch(); false}

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.items.clear()
                adapter.items.addAll(savedDistricts)
                adapter.filter { it.name?.contains(searchView.query.trim(), true) ?: false }
                adapter.notifyDataSetChanged()

                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })

    }

    override fun onStart() {
        super.onStart()
        districts.startLoading()
        savedDistricts = loadDistricts()

    }

    override fun onServiceReady() {
        adapter.items.clear()
        adapter.items.addAll(savedDistricts)
        adapter.filter { it.name?.contains(searchView.query.trim(), true) ?: false }
        adapter.notifyDataSetChanged()
        districts.stopLoading()
    }

    override fun onBackPressed() {
        val district = newStore.loadGlobal().district
        if (district != null) super.onBackPressed() // only accept back if district selected
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("titleVisibility", title.visibility)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.apply { title.visibility = getInt("titleVisibility") }
     }

    private fun onStartSearch() {
        title.visibility = View.GONE
    }

    private fun onEndSearch() {
        title.visibility = View.VISIBLE
    }

    private fun onDistrictClick(position: Int, district: District) {
        val global = newStore.loadGlobal().copy(district = district)
        Log.d("district", global.toString())
        newStore.saveGlobal(global)
        finish()
    }

    private fun loadDistricts(): List<District> {
        val districts = mutableListOf<District>()

        val districtsStr = getString(R.string.allAeries).trim()

        for (line in districtsStr.split("\n")) {
            try {
                val sections = line.trim().split(",")

                districts.add(District(sections[0].trim(), sections[1].trim(), sections[2].trim()))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        return districts
    }

    private lateinit var savedDistricts: List<District>

    lateinit var toolbar: Toolbar
    lateinit var searchView: SearchView
    lateinit var title: TextView

    lateinit var districts: MyList
    lateinit var adapter: DistrictsAdapter

}
