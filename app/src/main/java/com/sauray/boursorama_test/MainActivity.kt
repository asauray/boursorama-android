package com.sauray.boursorama_test

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.sauray.boursorama.Boursorama
import com.sauray.boursorama.model.Account
import com.sauray.boursorama_test.fragment.AccountFragment

/**
 * Created by Antoine Sauray on 22/01/2018.
 */

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, MenuItem.OnMenuItemClickListener {

    var selectedItem : MenuItem? = null

    var drawerToggle : ActionBarDrawerToggle? = null

    val bankAccounts : ArrayList<Account> = ArrayList()

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("MainActivity", "onNavigationItemSelected")
        return true
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {

        if (item != null) {
            if (item.groupId == R.id.group_accounts) {
                if(selectedItem != null) selectedItem!!.isChecked = false
                item.isChecked = true
                selectedItem = item
                bankAccounts[item.itemId].let {
                    switchToFragment(AccountFragment.getInstance(this, it))
                    title = it.name
                }
                findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
            }
        }
        return false
    }



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigationView = findViewById<NavigationView>(R.id.navigation)
        val subMenu = navigationView.menu.addSubMenu(R.id.group_accounts, 0, 0, R.string.bank_accounts)

        intent.extras.getParcelableArrayList<Account>(getString(R.string.accounts_key))?. let {
            val menus = ArrayList<MenuItem?>()
            for(i in 0 until it.size) {
                val account = it[i]
                bankAccounts.add(account)
                menus.add(addToMenu(subMenu, i, account))
            }
            menus[0]?. let { menuItem ->
                bankAccounts[0].let {
                    menuItem.isChecked = true
                    selectedItem = menuItem
                    switchToFragment(AccountFragment.getInstance(this, it))
                    title = it.name
                }
            }
        }

        val headerView = navigationView.inflateHeaderView(R.layout.view_header_navigation)


        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(drawerToggle!!)
    }

    fun addToMenu(menu: Menu, id: Int, account: Account): MenuItem? {
        val menuItem = menu.add(R.id.group_accounts, id, 0, account.name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            menuItem.icon = resources.getDrawable(R.drawable.ic_account_balance_black_24dp, theme)
        } else {
            @Suppress("DEPRECATION")
            menuItem.icon = resources.getDrawable(R.drawable.ic_account_balance_black_24dp)
        }
        menuItem.setOnMenuItemClickListener(this)
        return menuItem
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(drawerToggle != null) drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(drawerToggle != null) drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        Log.d("MainActivity", "onOptionsItemSelected")
        if(drawerToggle != null) {
            return if (drawerToggle!!.onOptionsItemSelected(item)) {
                true
            } else super.onOptionsItemSelected(item)
            // Handle your other action bar items...
        }
        return true
    }

    private fun switchToFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}
