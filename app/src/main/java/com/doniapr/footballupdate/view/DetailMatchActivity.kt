package com.doniapr.footballupdate.view

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doniapr.footballupdate.R
import com.doniapr.footballupdate.database.database
import com.doniapr.footballupdate.favorite.Favorite
import com.doniapr.footballupdate.model.LeagueDetail
import com.doniapr.footballupdate.model.Match
import com.doniapr.footballupdate.model.Team
import com.doniapr.footballupdate.presenter.MainPresenter
import com.doniapr.footballupdate.utility.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_match.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.onRefresh

class DetailMatchActivity : AppCompatActivity(), MainView {
    private lateinit var presenter: MainPresenter
    private lateinit var match: Match

    private var menuItem: Menu? = null
    private var isFavorite: Boolean = false
    private var eventId: Int = 0
    private var homeTeamBadge: String? = null
    private var awayTeamBadge: String? = null

    companion object {
        const val EVENT_ID: String = "event_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_match)

        setSupportActionBar(toolbar_detail_match)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        eventId = intent.getIntExtra(EVENT_ID, 0)

        favoriteState()

        presenter = MainPresenter(this)
        presenter.getMatchDetail(eventId.toString())

        swipe_refresh_detail_match.onRefresh {
            presenter.getMatchDetail(eventId.toString())
        }
    }

    override fun showLoading() {
        progress_bar_detail_match.visible()
    }

    override fun hideLoading() {
        progress_bar_detail_match.invisible()
    }

    override fun onFailed(message: String?) {
        swipe_refresh_detail_match.isRefreshing = false
        Toast.makeText(this@DetailMatchActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLeagueDetail(data: List<LeagueDetail>?) {
    }

    override fun showMatchList(data: List<Match>) {

    }

    override fun showMatchDetail(data: Match) {
        swipe_refresh_detail_match.isRefreshing = false
        supportActionBar?.title = data.eventName
        match = data

        Picasso.get().load(data.matchBanner + "/preview").into(img_banner_match)
        // Set Home
        txt_match_home.text = data.homeTeam
        txt_lineup_home.text = data.homeTeam
        // Match Result
        val round = data.leagueName + " " + resources.getString(R.string.round) + " " + data.round
        txt_match_round.text = round
        setDate(data)
        if (data.homeScore != null) {
            txt_match_home_score.text = data.homeScore.toString()
            txt_stats_home_goal.text = data.homeScore.toString()
        }
        if (data.homeGoalDetail != null || data.homeGoalDetail != "") {
            txt_home_goal_scorer.text = data.homeGoalDetail
        }
        //Stats
        if (data.homeShot != null) {
            txt_stats_home_shots.text = data.homeShot
        }
        if (data.homeRedCard != null || data.homeRedCard != "") {
            txt_stats_home_red_card.text = data.homeRedCard
        }
        if (data.homeYellowCard != null || data.homeYellowCard != "") {
            txt_stats_home_yellow_card.text = data.homeYellowCard
        }
        //Lineup
        if (data.homeGK != null || data.homeGK != "") {
            txt_lineup_gk_home.text = data.homeGK
        }
        if (data.homeDF != null || data.homeDF != "") {
            txt_lineup_df_home.text = data.homeDF
        }
        if (data.homeMF != null || data.homeMF != "") {
            txt_lineup_mf_home.text = data.homeMF
        }
        if (data.homeCF != null || data.homeCF != "") {
            txt_lineup_cf_home.text = data.homeCF
        }
        if (data.homeSubtitute != null || data.homeSubtitute != "") {
            txt_lineup_subs_home.text = data.homeSubtitute
        }

        // Set Away
        txt_match_away.text = data.awayTeam
        txt_lineup_away.text = data.awayTeam
        // Match Result
        if (data.awayScore != null) {
            txt_match_away_score.text = data.awayScore.toString()
            txt_stats_away_goal.text = data.awayScore.toString()
        }
        if (data.awayGoalDetail != null || data.awayGoalDetail != "") {
            txt_away_goal_scorer.text = data.awayGoalDetail
        }
        //Stats
        if (data.awayShot != null) {
            txt_stats_away_shots.text = data.awayShot
        }
        if (data.awayRedCard != null || data.awayRedCard != "") {
            txt_stats_away_red_card.text = data.awayRedCard
        }
        if (data.awayYellowCard != null || data.awayYellowCard != "") {
            txt_stats_away_yellow_card.text = data.awayYellowCard
        }
        //Lineup
        if (data.awayGK != null || data.awayGK != "") {
            txt_lineup_gk_away.text = data.awayGK
        }
        if (data.awayDF != null || data.awayDF != "") {
            txt_lineup_df_away.text = data.awayDF
        }
        if (data.awayMF != null || data.awayMF != "") {
            txt_lineup_mf_away.text = data.awayMF
        }
        if (data.awayCF != null || data.awayCF != "") {
            txt_lineup_cf_away.text = data.awayCF
        }
        if (data.awaySubtitute != null || data.awaySubtitute != "") {
            txt_lineup_subs_away.text = data.awaySubtitute
        }

        presenter.getTeamInfo(data.idHomeTeam.toString(), true)
        presenter.getTeamInfo(data.idAwayTeam.toString(), false)
    }

    override fun showTeam(data: Team, isHome: Boolean) {
        if (!data.teamBadge.isNullOrEmpty()) {
            if (isHome) {
                homeTeamBadge = data.teamBadge + "/preview"
                Picasso.get().load(data.teamBadge + "/preview").into(img_match_home_team_badge)
            } else {
                awayTeamBadge = data.teamBadge + "/preview"
                Picasso.get().load(data.teamBadge + "/preview").into(img_match_away_team_badge)
            }
        }
    }

    private fun setDate(match: Match) {
        if (!match.dateEvent.isNullOrEmpty() && !match.time.isNullOrEmpty()) {
            val utcDate = match.dateEvent.toString() + " " + match.time.toString()
            val wibDate = utcDate.toDateAndHour()
            val formatedDate = wibDate.formatTo("dd MMMM yyyy") + " " + wibDate.formatTo("HH:mm:ss")
            txt_match_detail_date.text = formatedDate
        } else if (!match.dateEvent.isNullOrEmpty() && match.time.isNullOrEmpty()) {
            val utcDate = match.dateEvent.toString()
            val wibDate = utcDate.toDate()
            txt_match_detail_date.text = wibDate.formatTo("dd MMMM yyyy")
        } else if (match.dateEvent.isNullOrEmpty() && !match.time.isNullOrEmpty()) {
            val utcDate = match.time.toString()
            val wibDate = utcDate.toHour()
            txt_match_detail_date.text = wibDate.formatTo("HH:mm:ss")

        } else {
            txt_match_detail_date.text = "-"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.favorite_menu, menu)
        menuItem = menu
        setFavorite()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_favorite -> {
                if (isFavorite) removeFromFavorite() else addToFavorite()

                isFavorite = !isFavorite
                setFavorite()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun addToFavorite(){
        try {
            database.use {
                insert(Favorite.TABLE_FAVORITE,
                    Favorite.EVENT_ID to match.eventId,
                    Favorite.EVENT_NAME to match.eventName,
                    Favorite.HOME_TEAM_NAME to match.homeTeam,
                    Favorite.AWAY_TEAM_NAME to match.awayTeam,
                    Favorite.HOME_TEAM_SCORE to match.homeScore.toString(),
                    Favorite.AWAY_TEAM_SCORE to match.awayScore.toString(),
                    Favorite.HOME_TEAM_BADGE to homeTeamBadge,
                    Favorite.AWAY_TEAM_BADGE to awayTeamBadge,
                    Favorite.LEAGUE_NAME to match.leagueName,
                    Favorite.ROUND to match.round.toString(),
                    Favorite.DATE to match.dateEvent,
                    Favorite.TIME to match.time
                )
            }
            layout_detail_container.snackbar("Berhasil menambahkan ke favorit").show()
        } catch (e: SQLiteConstraintException){
            layout_detail_container.snackbar(e.message.toString()).show()
        }
    }

    private fun removeFromFavorite(){
        try {
            database.use {
                delete(Favorite.TABLE_FAVORITE, "(${Favorite.EVENT_ID} = {id})",
                    "id" to eventId)
            }
            layout_detail_container.snackbar("Berhasil menghapus dari favorit").show()
        } catch (e: SQLiteConstraintException){
            layout_detail_container.snackbar(e.message.toString()).show()
        }
    }

    private fun setFavorite() {
        if (isFavorite)
            menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_added_24dp)
        else
            menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp)
    }

    private fun favoriteState(){
        database.use {
            val result = select(Favorite.TABLE_FAVORITE)
                .whereArgs("(${Favorite.EVENT_ID} = {id})",
                    "id" to eventId)
            val favorite = result.parseList(classParser<Favorite>())
            if (favorite.isNotEmpty()) isFavorite = true
        }
    }
}
