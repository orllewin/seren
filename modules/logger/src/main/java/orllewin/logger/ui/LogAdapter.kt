package orllewin.logger.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orllewin.logger.Logger
import orllewin.logger.R
import orllewin.logger.databinding.LogRowBinding

class LogAdapter(): RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    class ViewHolder(val binding: LogRowBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_row, parent, false)
        return ViewHolder(LogRowBinding.bind(view))
    }

    override fun getItemCount(): Int = Logger.logs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.logItem.text = Logger.logs.asReversed()[position]
    }
}