import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom';
import App from './pages/App';
import { Pages } from './configs/page.config';
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { SurveyList } from './pages/survey/SurveyList';
import { SurveyView } from './pages/survey/SurveyView';

export function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path={Pages.ROOT} element={<App />} />
                <Route path={Pages.AUTH}>
                    <Route path={Pages.AUTH_LOGIN} element={<Login />} />
                    <Route path={Pages.AUTH_REGISTER} element={<Register />} />
                </Route>

                <Route path={Pages.SURVEYS}>
                    <Route path={Pages.SURVEYS} element={<SurveyList />} />
                    <Route
                        element={
                            <div>
                                <header>Survey Actions</header>
                                <Outlet />
                            </div>
                        }
                    >
                        <Route path={Pages.SURVEYS_VIEW} element={<SurveyView />} />
                    </Route>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
