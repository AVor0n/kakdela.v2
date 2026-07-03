import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BreakpointProvider } from '@hh.ru/magritte-ui';
import { Provider } from 'react-redux';
import './index.css';
import { AppRouter } from './AppRouter.tsx';
import { store } from '@/entities/store.ts';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <Provider store={store}>
            <BreakpointProvider>
                <AppRouter />
            </BreakpointProvider>
        </Provider>
    </StrictMode>,
);
