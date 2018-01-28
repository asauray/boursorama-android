package com.sauray.boursorama_test.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sauray.boursorama.Boursorama
import com.sauray.boursorama.model.Account
import com.sauray.boursorama_test.Authentication
import com.sauray.boursorama_test.R
import com.sauray.boursorama_test.adapter.MovementAdapter

/**
 * Created by Antoine Sauray on 28/01/2018.
 */


class AccountFragment : Fragment() {

    private val TAG = "AccountFragment"
    private var account: Account? = null
    private var adapter : MovementAdapter? = null

    companion object {
        fun getInstance(context: Context, account: Account): AccountFragment {
            val f = AccountFragment()
            val bundle = Bundle()
            bundle.putParcelable(context.getString(R.string.account_key), account)
            f.arguments = bundle
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null) {
            arguments!!.getParcelable<Account>(getString(R.string.account_key))?. let {
                this.account = it
            } ?: throw IllegalArgumentException()
        } else {
            throw IllegalArgumentException()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = MovementAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter?. let { thisAdapter ->
            val credentials = Authentication(context).getCredentials(context)
            if(credentials != null) {
                Log.d(TAG, "got credentials")
                context?.let {
                    Boursorama.getInstance(it, credentials.first, credentials.second, {
                        Log.d(TAG, "got instance")
                        it.getMovements(this.account!!, {
                            Log.d(TAG, "got movements")
                            for(movement in it) {
                                Log.d(TAG, "movement: "+movement)
                                activity?.runOnUiThread({
                                    thisAdapter.add(movement)
                                })

                            }
                        }, {
                            Log.d(TAG, "movements failed")
                        })
                    }, {Log.d(TAG, "failed auth")})
                }
            }
        }

    }

}

