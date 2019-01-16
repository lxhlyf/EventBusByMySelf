/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.darren.architect_day22;


import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Posts events in background.
 * 
 * @author Markus
 */
class AsyncPoster implements Runnable {

    //1.存放了订阅方法，订阅方法的参数类型，以及Subscriber注解的参数
    Subscription subscription;
    //2.发送post请求是，post方法中传入的参数。
    Object event;

    //3.线程池
    /**
     * CachedThreadPool:
     * 1.是一个根据需要创建线程的线程池。
     * 2.corePoolSize : 0  ——五核心线程
     * 3.maximumPoolSize : Integer.MAX_VALUE  ——非核心线程是无界的
     * 4.keepAliveTime : 60L  ——空闲线程等待新任务的最长时间为60秒
     * 5.应用了阻塞队列SynchronousQueue,这是一个不储存元素的队列，每个
     * 插入操作必须等待另一个线程的移除操作，同样任何一个移除操作都等待另一个线程的插入操作。
     * 6.该线程的execute方法，首先会执行SynchronousQueue的offer方法来提交任务，并且询问
     * 现称此中是否有空闲的线程正等待着执行SynchronousQueue的poll方法来移除任务。如果有则
     * 配对成功，将任务交给这个空闲线程处理；如果没有配对成功，创建新的线程去处理任务。当一个线程在执行poll
     * 方法，60秒之后依旧没有新的任务提交的时候，该条等待线程就会被终止
     * 另外因为最大线程数(maximumPoolSize)是无界的，为了保证处理的速度，当提交的任务处理速度大于一定值，
     * 就会创建新的线程去处理任。每次提交的任务都会立即有线程去处理。
     *7.该类线程池，适合于（大量的）需要（立即）处理并且（耗时较少）的任务。
     */
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    public AsyncPoster(Subscription subscription, Object event){
        this.subscription = subscription;
        this.event = event;
    }


    public static void enqueue(Subscription subscription, Object event) {
        //通过构造方法，准备好参数
        AsyncPoster asyncPoster = new AsyncPoster(subscription,event);
        // 启用线程池，需要传入一个实现了Runnable接口的实现类
        executorService.execute(asyncPoster);
    }

    @Override
    public void run() {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber,event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
