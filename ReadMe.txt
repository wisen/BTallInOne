1. 
date: 2016-07-21
root cause:
		��init_hank_sequence��һ���Է���̫����request,����С���ֻ���ɲ���,��ɴ���:
		writeCharacteristic: mDeviceBusy = true, and return false
		��spec��˵��ÿһ��request����command������ԭ�Ӳ���,ͬһʱ��ֻ�����һ������,������ԭ��:
        /*An attribute protocol request and response or indication-confirmation pair is
        considered a single transaction. A transaction shall always be performed on
        one ATT Bearer, and shall not be split over multiple ATT Bearers.*/
        //����Ŀǰ���ּ���ʱ����������������, Ӧ����һ��queue, ��request�ͼӵ�queue��
        //����ǰ�����������,��ִ����һ�������request
next action:
		���һ��queue�����������е�request, ����ǰ��������ɺ�, ��queue����ȡ����һ��requestִ��

2.
date: 2016-07-21
root cause:
		Ŀǰû��״̬������, С�׶��ߺ�, �����Ҫ�������С��, ��Ӧ������, ��������Ӧ��Ҫ�жϵ�ǰ��״̬
		������Ƕ�����, ������, û����, ��ô�����������ǵ�request
next action:
		���һ��BtSmartServiceStateMachine, ���״̬����Ŀǰ��״̬,ÿһ������Ӧ���жϵ�ǰ��״̬
		
3. 
date: 2016-07-28
��¼һ��issue, Ŀǰ�İ汾��update_item��д��contact��,Ȼ��ӶԶ�����pull phone book�����ҵ��ղ�д���contact
���������update_all������д��contacts��, Ȼ��ӶԶ�����pull phone book���Ҳ����ղ�����д���contacts. ԭ�����..
     