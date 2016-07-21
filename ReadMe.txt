1. 
date: 2016-07-21
root cause:
		在init_hank_sequence中一次性发了太多了request,导致小米手环完成不了,造成错误:
		writeCharacteristic: mDeviceBusy = true, and return false
		而spec上说了每一个request或者command都属于原子操作,同一时间只能完成一次事务,下面是原话:
        /*An attribute protocol request and response or indication-confirmation pair is
        considered a single transaction. A transaction shall always be performed on
        one ATT Bearer, and shall not be split over multiple ATT Bearers.*/
        //所以目前这种加延时的做法还是有问题, 应该做一个queue, 有request就加到queue中
        //当当前的事务完成了,再执行下一个事务的request
next action:
		添加一个queue用来存在所有的request, 当当前的事务完成后, 从queue里面取出下一个request执行

2.
date: 2016-07-21
root cause:
		目前没有状态机机制, 小米断线后, 如果需要发请求给小米, 就应该重连, 而重连就应该要判断当前的状态
		如果你是断线了, 就重连, 没断线, 那么继续发送我们的request
next action:
		添加一个BtSmartServiceStateMachine, 这个状态更新目前的状态,每一个请求都应该判断当前的状态
     