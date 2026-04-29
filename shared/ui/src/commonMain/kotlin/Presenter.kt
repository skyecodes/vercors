package app.vercors.shared.ui

import androidx.compose.runtime.Stable
import com.slack.circuit.runtime.presenter.Presenter as CircuitPresenter

interface Presenter<S, E : Event> : CircuitPresenter<Store<S, E>> {
    @Stable
    infix operator fun <S, E : Event> S.plus(eventSink: EventSink<E>): Store<S, E> = Store(this, eventSink)
}
