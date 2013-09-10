package onyx.core.util

import java.util.concurrent.atomic.{AtomicReference}
import scala.ref.WeakReference
import scala.util.DynamicVariable

/**
 * Stripped down code from scala.rx, because scala.rx is not supported for scala 2.9.3
 */

trait Node {
  def level: Long

  def name: String
}

trait Emitter[+T] extends Node {
  private[this] val children = new AtomicReference[List[WeakReference[Reactor[T]]]](Nil)

  def childrens: Seq[Reactor[Nothing]] = children.get.flatMap(_.get)
  def linkChild[R >: T](child: Reactor[R]): Unit = {
    def update: Boolean = {
      val oldV = children.get
      val newV = (new WeakReference(child)) :: (oldV.filter(_.get.isDefined).distinct)
      children.compareAndSet(oldV, newV)
    }
    while(!update){}
  }
}

trait Reactor[-T] extends Node {
  def parents: Seq[Emitter[Any]]
  def ping(emitters: Seq[Emitter[Any]]): Seq[Reactor[Nothing]]
}

object Reactive {
  val enclosing = new DynamicVariable[Option[(Reactive[Any], List[Reactive[Any]])]](None)

  def apply[T](body: => T, name: String = ""): Reactive[T] =
    new Reactive(() => body, name)
}

class Reactive[+T](body: () => T, val name: String = "") extends Emitter[T] with Reactor[Any] {

  class State(val value: Option[T], val level: Long, val parents: Seq[Emitter[Any]])

  def parents = state.get.parents
  def level: Long = state.get.level

  protected var state = new AtomicReference(run)

  protected[this] def currentValue: T = state.get.value.get
  def now: T = currentValue

  private def run: State = {
    Reactive.enclosing.withValue(Some(this -> Nil)) {
      body()
      val deps = Reactive.enclosing.value.get._2
      new State(Some(body()), ((0L :: deps.map(_.level)).max + 1L), deps)
    }
  }

  def apply(): T = {
    Reactive.enclosing.value = Reactive.enclosing.value match {
      case Some((enclosing, dependencies)) =>
        this.linkChild(enclosing)
        Some((enclosing, this :: dependencies))
      case None => None
    }
    currentValue
  }

  def propagate() = {
    new Immediate propagate this.childrens.map(this -> _)
  }

  def ping(emitters: Seq[Emitter[Any]]): Seq[Reactor[Nothing]] = {
    if(!parents.intersect(emitters).isEmpty){
      state.set(run)
      childrens
    } else Nil
  }
}

object Var {
  def apply[T](initVal: => T, name: String = "") = new Var(initVal, name)
}

class Var[T](initVal: => T, val _name: String = "") extends Reactive[T](() => initVal, _name) {

  override def level = 0L

  def update(newVal: => T) = {
    state.set(new State(Some(newVal), state.get.level, state.get.parents))
    propagate()
  }
}

object Observe {
  def apply(source: Emitter[Any], name: String = "")(body: => Unit) = new Observer(source, () => body, name)
}

class Observer(source: Emitter[Any],
               body: () => Unit,
               val name: String = ""
              ) extends Reactor[Any] {

  source.linkChild(this)
  def parents = Seq(source)
  def level = Long.MaxValue

  def ping(emitters: Seq[Emitter[Any]]): Seq[Reactor[Nothing]] = {
    if(!parents.intersect(emitters).isEmpty){
      body()
    }
    Nil
  }
}

class Immediate {
  def propagate(iniNodes: Seq[(Emitter[Any], Reactor[Nothing])]): Unit = {
    if(iniNodes.length != 0){
      var nodes = iniNodes
      def continue: Boolean = {
        val minL = nodes.map(_._2.level).min
        val (now, later) = nodes.partition(_._2.level == minL)
        nodes = now.groupBy(_._2)
                   .mapValues(_.map(_._1).distinct)
                   .toSeq
                   .map({ n =>
          n._1.ping(n._2).map(n._1.asInstanceOf[Emitter[Any]] -> _)
        }).flatten ++ later
        nodes.length != 0
      }

      while(continue){}
    }
  }
}