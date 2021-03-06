package com.tonyyang.github.users.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.tonyyang.github.users.ExecuteOnceObserver
import com.tonyyang.github.users.api.GithubService
import com.tonyyang.github.users.model.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class UserDataSource(
        private val githubService: GithubService,
        private val query: String
) : PageKeyedDataSource<Int, User>() {

    val initialLoad by lazy {
        MutableLiveData<NetworkState>()
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, User>) {
        initialLoad.postValue(NetworkState.LOADING)
        githubService.searchUsers(query, PER_PAGE_SIZE, FIRST_PAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ExecuteOnceObserver(onExecuteOnceNext = {
                    callback.onResult(it.items, null, FIRST_PAGE + 1)
                    initialLoad.postValue(NetworkState.LOADED)
                }, onExecuteOnceError = {
                    initialLoad.postValue(NetworkState.ERROR)
                    it.message?.let { message ->
                        Log.e(TAG, message)
                    }
                }))
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
        initialLoad.postValue(NetworkState.LOADING)
        githubService.searchUsers(query, PER_PAGE_SIZE, params.key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ExecuteOnceObserver(onExecuteOnceNext = {
                    callback.onResult(it.items, params.key + 1)
                    initialLoad.postValue(NetworkState.LOADED)
                }, onExecuteOnceError = {
                    initialLoad.postValue(NetworkState.ERROR)
                    it.message?.let { message ->
                        Log.e(TAG, message)
                    }
                }))
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
        // do nothing
    }

    companion object {
        private val TAG = UserDataSource::class.java.simpleName
        private const val FIRST_PAGE = 1
        const val PER_PAGE_SIZE = 15
    }
}