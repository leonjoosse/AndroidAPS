package info.nightscout.androidaps.plugins.general.automation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import info.nightscout.androidaps.R
import info.nightscout.androidaps.plugins.bus.RxBus
import info.nightscout.androidaps.plugins.general.automation.dialogs.EditEventDialog
import info.nightscout.androidaps.plugins.general.automation.events.EventAutomationDataChanged
import info.nightscout.androidaps.plugins.general.automation.events.EventAutomationUpdateGui
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.automation_fragment.*

class AutomationFragment : Fragment() {

    private var disposable: CompositeDisposable = CompositeDisposable()
    private var eventListAdapter: EventListAdapter? = null

    operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.automation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventListAdapter = EventListAdapter(AutomationPlugin.automationEvents, fragmentManager)
        automation_eventListView.layoutManager = LinearLayoutManager(context)
        automation_eventListView.adapter = eventListAdapter

        automation_fabAddEvent.setOnClickListener {
            val dialog = EditEventDialog()
            val args = Bundle()
            args.putString("event", AutomationEvent().toJSON())
            args.putInt("position", -1) // New event
            dialog.arguments = args
            fragmentManager?.let { dialog.show(it, "EditEventDialog") }
        }

        disposable += RxBus
                .toObservable(EventAutomationUpdateGui::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    eventListAdapter?.notifyDataSetChanged()
                    val sb = StringBuilder()
                    for (l in AutomationPlugin.executionLog) {
                        sb.append(l)
                        sb.append("\n")
                    }
                    automation_logView.text = sb.toString()
                }, {})
        disposable += RxBus
                .toObservable(EventAutomationDataChanged::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    eventListAdapter?.notifyDataSetChanged()
                }, {})
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

}
