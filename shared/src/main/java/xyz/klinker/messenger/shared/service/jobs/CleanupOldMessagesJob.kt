package xyz.klinker.messenger.shared.service.jobs

import android.content.Context
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.SimpleJobService
import xyz.klinker.messenger.shared.data.DataSource
import xyz.klinker.messenger.shared.data.Settings
import xyz.klinker.messenger.shared.util.TimeUtils
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.Trigger
import com.firebase.jobdispatcher.Lifetime

class CleanupOldMessagesJob : SimpleJobService() {
    override fun onRunJob(job: JobParameters?): Int {
        val timeout = Settings.cleanupMessagesTimeout
        if (timeout > 0) {
            DataSource.cleanupOldMessages(this, TimeUtils.now - timeout)
        }

        scheduleNextRun(this)
        return 0
    }

    companion object {
        private val JOB_ID = "cleanup-old-messages"

        fun scheduleNextRun(context: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            val time = (TimeUtils.millisUntilHourInTheNextDay(3).toLong() / 1000).toInt()
            val myJob = dispatcher.newJobBuilder()
                    .setService(CleanupOldMessagesJob::class.java)
                    .setTag(JOB_ID)
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(time, time + (TimeUtils.MINUTE.toInt() / 1000 * 5)))
                    .setReplaceCurrent(true)
                    .build()

            dispatcher.mustSchedule(myJob)
        }
    }
}
