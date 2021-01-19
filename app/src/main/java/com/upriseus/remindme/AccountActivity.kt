package com.upriseus.remindme

import com.upriseus.remindme.layout_features.MaterialNavActivity

class AccountActivity : MaterialNavActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_account
    }

    override fun getItemId(): Int {
        return R.id.account_item
    }

}