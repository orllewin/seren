package orllewin.history.ui

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orllewin.history.R
import orllewin.history.databinding.HistoryRowBinding
import orllewin.history.db.HistoryEntry

class HistoryAdapter(val history: List<HistoryEntry>, val onClick:(entry: HistoryEntry) -> Unit): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: HistoryRowBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_row, parent, false)
        return ViewHolder(HistoryRowBinding.bind(view))
    }

    override fun getItemCount(): Int = history.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyEntry = history[position]

        when {
            historyEntry.title.isNullOrEmpty() -> {
                //no title
                holder.binding.historyTitle.text = historyEntry.uri.toString().replace("gemini://", "")
                holder.binding.historyAddress.text = ""
            }
            else -> {
                holder.binding.historyTitle.text = historyEntry.title
                holder.binding.historyAddress.text = historyEntry.uri.toString().replace("gemini://", "")
            }
        }

        holder.binding.historyRow.setOnClickListener {
            delay(500){
                onClick(history[holder.adapterPosition])
            }
        }
    }

    fun delay(ms: Long, action: () -> Unit){
        object : CountDownTimer(ms, ms/2) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                action.invoke()
            }
        }.start()
    }
}