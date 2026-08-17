export function createRequestSemaphore(limit: number) {
    let activeRequestCount = 0;
    const waiters: Array<() => void> = [];

    const acquire = async () => {
        if (activeRequestCount < limit) {
            activeRequestCount += 1;
            return;
        }
        await new Promise<void>((resolve) => {
            waiters.push(resolve);
        });
    };

    const release = () => {
        const nextWaiter = waiters.shift();
        if (nextWaiter) nextWaiter();
        else activeRequestCount = Math.max(0, activeRequestCount - 1);
    };

    return async <T>(request: () => Promise<T>) => {
        await acquire();
        try {
            return await request();
        } finally {
            release();
        }
    };
}
