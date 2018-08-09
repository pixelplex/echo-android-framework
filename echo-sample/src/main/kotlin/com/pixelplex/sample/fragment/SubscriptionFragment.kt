package com.pixelplex.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.sample.R
import kotlinx.android.synthetic.main.fragment_subscription.*

/**
 * @author Daria Pechkovskaya
 */
class SubscriptionFragment : BaseFragment() {

    companion object {
        fun newInstance(): SubscriptionFragment {
            return SubscriptionFragment()
        }
    }

    private lateinit var accountListener: AccountListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_subscription, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        accountListener = object : AccountListener {
            override fun onChange(updatedAccount: Account) {
                updateStatus(updatedAccount.toString())
            }
        }

        btnSubscribe.setOnClickListener {
            progressListener?.toggle(true)
            lib?.subscribeOnAccount(
                etName.text.toString(),
                accountListener,
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        progressListener?.toggle(false)
                        updateStatus("Subscribe succeed")
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        progressListener?.toggle(false)
                        updateStatus("Subscribe failed")
                    }
                })
        }

        btnUnsubscribe.setOnClickListener {
            progressListener?.toggle(true)
            lib?.unsubscribeFromAccount(etName.text.toString(), object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe succeed")
                }

                override fun onError(error: LocalException) {
                    error.printStackTrace()
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe failed")
                }

            })
        }

        btnUnsubscribeAll.setOnClickListener {
            progressListener?.toggle(true)
            lib?.unsubscribeAll(object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe all succeed")
                }

                override fun onError(error: LocalException) {
                    error.printStackTrace()
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe all failed")
                }

            })
        }
    }

    override val tvStatus: TextView?
        get() = txtStatus

    override fun clear() {
        etName.text.clear()
    }
}