package rpost.jpalabs;

import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

public class TxHelper {

    @Transactional
    public void doInTransaction(Runnable runnable) {
        runnable.run();
    }

    @Transactional
    public <T> T doInTransaction(Supplier<T> supplier) {
        return supplier.get();
    }
}
