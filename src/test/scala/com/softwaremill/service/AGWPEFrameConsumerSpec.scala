package com.softwaremill.service

import java.io.DataInputStream
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import com.softwaremill.FrameUtils
import com.softwaremill.agwpe.AGWPEFrame
import com.softwaremill.ax25.AX25Frame
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class AGWPEFrameConsumerSpec extends FlatSpec with Matchers with BeforeAndAfter {

  "An AGWPEFrameConsumer" should "process given element in the queue" in {
    //given
    val queue: BlockingQueue[AGWPEFrame] = new LinkedBlockingQueue[AGWPEFrame]
    val consumer: AGWPEFrameConsumer = new AGWPEFrameConsumer(queue)
    val observer: TestObserver = new TestObserver
    consumer.addObserver(observer)
    val dis: DataInputStream = FrameUtils.dataStream("/dataFrame.bin")
    val frame: AGWPEFrame = AGWPEFrame(dis)
    //when
    consumer.consume(frame)
    //then
    observer.observerCalled shouldBe true
  }

  "An AGWPEFrameConsumer" should "consume elements from the queue" in {
    //given
    val queue: BlockingQueue[AGWPEFrame] = new LinkedBlockingQueue[AGWPEFrame]
    val consumer: AGWPEFrameConsumer = new AGWPEFrameConsumer(queue)
    val dis: DataInputStream = FrameUtils.dataStream("/dataFrame.bin")
    val frame: AGWPEFrame = AGWPEFrame(dis)
    val observer: TestObserver = new TestObserver
    consumer.addObserver(observer)
    val thread: Thread = new Thread(consumer)
    //when
    thread.start()
    sleep()
    queue.put(frame)
    sleep()
    //then
    queue.isEmpty shouldBe true
    observer.observerCalled shouldBe true
    thread.interrupt()
  }

  def sleep(): Unit = {
    val HalfASecond = 500
    try {
      Thread.sleep(HalfASecond)
    } catch {
      case ie: InterruptedException =>
    }
  }
}

class TestObserver extends FrameObserver[AX25Frame] {
  var observerCalled: Boolean = false

  override def receiveUpdate(subject: AX25Frame): Unit = {
    this.observerCalled = true
  }
}