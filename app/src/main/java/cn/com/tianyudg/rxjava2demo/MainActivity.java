package cn.com.tianyudg.rxjava2demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.com.tianyudg.rxjava2demo.util.LogUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private ArrayList<List<String>> outList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tvText);
        outList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            ArrayList<String> inList = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                inList.add("inList - " + j);
            }
            outList.add(inList);
        }
    }


    public void basicTestClick(View view) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {

                emitter.onNext("one");
                emitter.onNext("two");
                emitter.onNext("three");
                emitter.onNext("four");
                emitter.onComplete();

            }
        }).subscribe(new Observer<String>() {

            Disposable mDisposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(@NonNull String s) {
                if ("three".equals(s) && !mDisposable.isDisposed()) {
                    mDisposable.dispose();
                }
                LogUtils.e(" onNext  s = " + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                LogUtils.e(" onError    e = " + e.getMessage());
            }

            @Override
            public void onComplete() {
                LogUtils.e(" onComplete");
            }
        });
    }


    public void consumerTestClick(View view) {
        Observable
                .just(1, 2, 23, 4)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                        LogUtils.e(" doOnNext integer =" + integer + " - - - Thread = " + Thread.currentThread().getName());
                    }
                })
//                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e(" subscribe integer =" + integer + " - - - Thread = " + Thread.currentThread().getName());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(" Error   msg :　" + throwable.getMessage());
                    }
                });
    }

    public void concatTestClick(View view) {
        Observable<String> localData = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                LogUtils.e(" localData ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());

                if (getLocalDataSuccessful()) {
                    emitter.onNext("localData...");
                } else {
                    emitter.onComplete();
                }
            }
        });


        Observable<String> networkData = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("networkData...");
                LogUtils.e(" networkData - - ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());
            }
        });


        Observable
                .concat(localData, networkData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtils.e("concat  s = " + s + " - - - Thread = " + Thread.currentThread().getName());
                    }
                });


    }

    private boolean getLocalDataSuccessful() {

        return true;
    }


    public void zipTestClick(View view) {
        Observable<Integer> integer = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(999);
                LogUtils.e(" Integer - - ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());
            }
        });

        Observable<String> str = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("String");
                LogUtils.e(" String - - ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());
            }
        });

        Observable
                .zip(integer, str, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                LogUtils.e(" zip - - ("+s+integer+") - - - Thread = " + Thread.currentThread().getName());
                return s+" + integer "+integer;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                LogUtils.e(" subscribe - - Consumer  final result="+s+" - - - Thread = " + Thread.currentThread().getName());
            }
        });


    }

    public void intervalTestClick(View view) {
        Observable
                .interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())//不起作用，默认运行在RxComputationThreadPool线程
//                .observeOn(AndroidSchedulers.mainThread())//这个起作用
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        LogUtils.e(" Consumer aLong="+aLong+"- - - Thread = " + Thread.currentThread().getName());
                    }
                });
    }
}
