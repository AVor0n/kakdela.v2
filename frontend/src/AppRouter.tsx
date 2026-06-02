import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom';
import App from './pages/App';
import { Pages } from './configs/page.config';
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { SurveyList } from './pages/survey/SurveyList';
import { SurveyView } from './pages/survey/SurveyView';
import { SurveyModify } from './pages/survey/SurveyModify';
import { SurveyCreate } from './pages/survey/SurveyCreate';
import { SurveyLayout } from './layouts/SurveyLayout';

export function AppRouter() {
    return (
        <BrowserRouter>
            {/* root routes */}
            <Routes>
                <Route path={Pages.ROOT} element={<App />} />
            </Routes>
            {/* Auth routes */}
            <Routes>
                <Route path={Pages.AUTH}>
                    <Route path={Pages.AUTH_LOGIN} element={<Login />} />
                    <Route path={Pages.AUTH_REGISTER} element={<Register />} />
                </Route>
            </Routes>

            {/* Survey routes */}
            <Routes>
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
                        <Route element={<SurveyLayout />}>
                            <Route path={Pages.SURVEYS_EDIT} element={<SurveyModify />}>
                                <Route path='settings' element={<div>Settings</div>} />
                                <Route path='questions' element={<div>Questions</div>} />
                                <Route path='answers' element={<div>Answers</div>} />
                            </Route>
                            <Route path={Pages.SURVEYS_CREATE} element={<SurveyCreate />}>
                                <Route path='settings' element={<div>Settings</div>} />
                                <Route path='questions' element={<div>Questions</div>} />
                                <Route path='answers' element={<div>Answers</div>} />
                            </Route>
                        </Route>
                    </Route>
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
