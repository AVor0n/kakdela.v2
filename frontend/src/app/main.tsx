import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BreakpointProvider } from '@hh.ru/magritte-ui';
import './index.css';
import { AppRouter } from './AppRouter.tsx';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <BreakpointProvider>
            <AppRouter />
        </BreakpointProvider>
    </StrictMode>,
);
