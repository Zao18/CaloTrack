package student.projects.calotrack // The correct package name for this file

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.calotrack.R
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

data class DailySummary(
    val date: String,
    val dateKey: String,
    val consumedCalories: Int,
    val goalCalories: Int
)
data class FoodLogEntry(
    val calories: Int = 0,
    val category: String = "",
    val name: String = ""
)

class HistoryActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var tvLoading: TextView
    private var calorieGoal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        tvLoading = findViewById(R.id.tvLoading)

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.visibility = View.GONE
        tvLoading.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showErrorState("Error: User not logged in.")
            return
        }
        database = FirebaseDatabase.getInstance().getReference("users").child(userId)

        loadCalorieHistory()
    }
    private fun loadCalorieHistory() {
        val userRef = database

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.e("HistoryActivity", "User data not found.")
                    showErrorState("User data not found.")
                    return
                }
                calorieGoal = snapshot.child("calorie_goal").getValue(Int::class.java) ?:
                        snapshot.child("calculated_goal").getValue(Int::class.java) ?: 2000
                val foodLogSnapshot = snapshot.child("food_log")
                if (!foodLogSnapshot.exists()) {
                    showErrorState(getString(R.string.no_history))
                    return
                }

                val dailySummaries = processFoodLog(foodLogSnapshot)

                if (dailySummaries.isNotEmpty()) {
                    val adapter = DailySummaryAdapter(dailySummaries)
                    historyRecyclerView.adapter = adapter
                    historyRecyclerView.visibility = View.VISIBLE
                    tvLoading.visibility = View.GONE
                } else {
                    showErrorState(getString(R.string.no_history))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryActivity", "Database error: ${error.message}")
                showErrorState("Failed to load history: ${error.message}")
            }
        })
    }

    private fun processFoodLog(foodLogSnapshot: DataSnapshot): List<DailySummary> {
        val dailyTotals = mutableMapOf<String, Int>()

        val displayFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val groupingFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (logEntrySnapshot in foodLogSnapshot.children) {
            val timestampKey = logEntrySnapshot.key
            val foodEntry = logEntrySnapshot.getValue(FoodLogEntry::class.java)

            if (timestampKey != null && foodEntry != null) {
                val timestamp = timestampKey.toLong()
                val date = Date(timestamp)

                val dateKey = groupingFormatter.format(date)

                val currentTotal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dailyTotals.getOrDefault(dateKey, 0)
                } else {
                    dailyTotals[dateKey] ?: 0
                }
                dailyTotals[dateKey] = currentTotal + foodEntry.calories
            }
        }

        return dailyTotals.map { (dateKey, consumed) ->
            val date = groupingFormatter.parse(dateKey)!!
            DailySummary(
                date = displayFormatter.format(date),
                dateKey = dateKey,
                consumedCalories = consumed,
                goalCalories = calorieGoal
            )
        }.sortedByDescending { it.dateKey }
    }

    private fun showErrorState(message: String) {
        tvLoading.text = message
        tvLoading.visibility = View.VISIBLE
        historyRecyclerView.visibility = View.GONE
    }

    private inner class DailySummaryAdapter(private val summaries: List<DailySummary>)
        : RecyclerView.Adapter<DailySummaryAdapter.DailySummaryViewHolder>() {

        inner class DailySummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvSummaryDate)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            val tvSummary: TextView = itemView.findViewById(R.id.tvCalorieSummary)
            val progressBar: ProgressBar = itemView.findViewById(R.id.dailyProgressBar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailySummaryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_daily_summary,
                parent,
                false
            )
            return DailySummaryViewHolder(view)
        }

        override fun onBindViewHolder(holder: DailySummaryViewHolder, position: Int) {
            val item = summaries[position]

            holder.tvDate.text = item.date
            holder.tvSummary.text = "${item.consumedCalories} / ${item.goalCalories} kcal"

            val progressPercent = if (item.goalCalories > 0) {
                val rawPercent = (item.consumedCalories.toFloat() / item.goalCalories) * 100
                rawPercent.coerceAtMost(100f).toInt()
            } else {
                0
            }

            holder.progressBar.progress = progressPercent

            val context = holder.itemView.context
            if (item.consumedCalories <= item.goalCalories) {
                val remaining = item.goalCalories - item.consumedCalories
                holder.tvStatus.text = context.getString(R.string.goal_reached, remaining)
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green_accent))
                holder.progressBar.progressTintList = ContextCompat.getColorStateList(context, R.color.green_accent)
            } else {
                val excess = item.consumedCalories - item.goalCalories
                holder.tvStatus.text = context.getString(R.string.goal_exceeded, excess)
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_warning))
                holder.progressBar.progressTintList = ContextCompat.getColorStateList(context, R.color.red_warning)
            }
        }

        override fun getItemCount(): Int = summaries.size
    }
}