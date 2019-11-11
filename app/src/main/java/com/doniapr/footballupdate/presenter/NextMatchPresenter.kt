package com.doniapr.footballupdate.presenter

import android.content.Context
import com.doniapr.footballupdate.R
import com.doniapr.footballupdate.apiservice.MainApi
import com.doniapr.footballupdate.model.MatchResponse
import com.doniapr.footballupdate.view.NextMatchView
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NextMatchPresenter(
    private val view: NextMatchView,
    private val context: Context?
) {
    fun getNextMatch(leagueId: Int?) {
        view.showLoading()
        doAsync {
            MainApi().services.getNextMatch(leagueId.toString()).enqueue(object :
                Callback<MatchResponse> {
                override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                    view.onFailed(context?.getString(R.string.no_internet))
                    view.hideLoading()

                }

                override fun onResponse(
                    call: Call<MatchResponse>,
                    response: Response<MatchResponse>
                ) {
                    if (response.code() == 200) {
                        response.body()?.matches.let {
                            if (!it.isNullOrEmpty()) {
                                view.hideLoading()
                                view.showMatchList(it)
                            } else {
                                view.hideLoading()
                                view.onFailed(context?.getString(R.string.no_data))
                            }
                        }
                    } else {
                        view.hideLoading()
                        view.onFailed(context?.getString(R.string.no_data))

                    }
                }
            })
        }
    }
}