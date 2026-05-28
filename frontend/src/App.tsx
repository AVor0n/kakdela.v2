import { Skeleton } from '@hh.ru/magritte-ui';
import './App.css';

function App() {
    return (
        <>
            <main>
                <Skeleton width={350} height={100} loading={true} />
                <p>KakDelaV2.0? В разработке...</p>
            </main>
        </>
    );
}

export default App;
