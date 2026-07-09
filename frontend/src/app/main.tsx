import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BreakpointProvider } from '@hh.ru/magritte-ui';
import { Provider } from 'react-redux';
import './index.css';
import { AppRouter } from './AppRouter.tsx';
import { store } from '@/entities/store.ts';
import { ErrorLayout } from '@/layouts/ErrorLayout/ErrorLayout.tsx';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <Provider store={store}>
            <BreakpointProvider>
                <ErrorLayout>
                    <AppRouter />
                </ErrorLayout>
            </BreakpointProvider>
        </Provider>
    </StrictMode>,
);
