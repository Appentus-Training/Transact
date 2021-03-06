package com.sayantanbanerjee.transactionmanagementapp.presenter.TransactionActivityPackage

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.sayantanbanerjee.transactionmanagementapp.R
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sayantanbanerjee.transactionmanagementapp.NetworkConnectivity
import com.sayantanbanerjee.transactionmanagementapp.databinding.ActivityTransactionBinding
import com.sayantanbanerjee.transactionmanagementapp.presenter.AddRecordActivityPackage.AddRecordActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionBinding

    @Inject
    lateinit var factory: TransactionViewModelFactory

    @Inject
    lateinit var transactionAdapter: TransactionAdapter

    lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction)
        viewModel = ViewModelProvider(this, factory).get(TransactionViewModel::class.java)

        initRecyclerView()
        viewDefaultList()
        setSearchView()

        binding.updateFloatingActionButton.setOnClickListener {
            if (NetworkConnectivity.isNetworkAvailable(this)) {
                viewModel.getStateFromFirebase()
                Toast.makeText(this, "Transactions are updated!", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(this, "Network connectivity isn\'t available.", Toast.LENGTH_LONG)
                    .show()
            }

        }

    }

    private fun initRecyclerView() {
        binding.transactionRecyclerView.adapter = transactionAdapter
        binding.transactionRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @SuppressLint("SetTextI18n")
            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.personView.text = "TRANSACTIONS OF : $p0"
                viewSearchedList(p0!!)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })

        binding.searchView.setOnCloseListener {
            binding.personView.text = getString(R.string.defaultGroupPerson)
            initRecyclerView()
            viewDefaultList()
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun viewDefaultList() {
        viewModel.getSumSentAcceptedValue().observe(this, Observer {
            binding.acceptedSentSum.text = "YOU GAVE (VERIFIED) : Rs. $it"
        })

        viewModel.getSumReceivedValue().observe(this, Observer {
            binding.receivedSum.text = "YOU RECEIVED (VERIFIED) : Rs. $it"
        })

        viewModel.getAllRecords().observe(this, {
            transactionAdapter.differ.submitList(it)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun viewSearchedList(phoneNumber: String) {
        viewModel.getAcceptedSumSentOfAParticularContact(phoneNumber).observe(this, Observer {
            binding.acceptedSentSum.text = "YOU GAVE (VERIFIED) : Rs. $it"
        })

        viewModel.getAcceptedSumReceivedOfAParticularContact(phoneNumber).observe(this, Observer {
            binding.receivedSum.text = "YOU RECEIVED (VERIFIED) : Rs. $it"
        })

        viewModel.getAllTransactionsRecordOfAParticularContact(phoneNumber).observe(this, {
            transactionAdapter.differ.submitList(it)
        })
    }
}
