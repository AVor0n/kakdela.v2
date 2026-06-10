import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom';
import App from '@/pages/Root/App';
import { routePatterns } from '@/app/routes';
import { Login } from '@/pages/Auth/Login/Login';
import { Register } from '@/pages/Auth/Register/Register';
import { SurveyList } from '@/pages/Survey/components/SurveyList/SurveyList';
import { SurveyView } from '@/pages/Survey/SurveyView/SurveyView';
import { SurveyModify } from '@/pages/Survey/SurveyModify/SurveyModify';
import { SurveyCreate } from '@/pages/Survey/SurveyCreate/SurveyCreate';
import { SurveyLayout } from '@/layouts/SurveyLayout';
import { RequireAuth } from '@/features/auth/RequireAuth';

export function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path={routePatterns.root} element={<App />} />
                <Route path={routePatterns.auth}>
                    <Route path={routePatterns.authLogin} element={<Login />} />
                    <Route path={routePatterns.authRegister} element={<Register />} />
                </Route>

                <Route path={routePatterns.surveys}>
                    <Route
                        path={routePatterns.surveys}
                        element={
                            <RequireAuth>
                                <SurveyList />
                            </RequireAuth>
                        }
                    />
                    <Route
                        element={
                            <div>
                                <header>Survey Actions</header>
                                <Outlet />
                            </div>
                        }
                    >
                        <Route path={routePatterns.surveysView} element={<SurveyView />} />
                        <Route element={<SurveyLayout />}>
                            <Route
                                path={routePatterns.surveyModify}
                                element={
                                    <RequireAuth>
                                        <SurveyModify />
                                    </RequireAuth>
                                }
                            >
                                <Route path='settings' element={<div>Settings</div>} />
                                <Route path='questions' element={<SurveyModify />} />
                                <Route path='answers' element={<div>Answers</div>} />
                            </Route>
                            <Route
                                path={routePatterns.surveyCreate}
                                element={
                                    <RequireAuth>
                                        <SurveyCreate />
                                    </RequireAuth>
                                }
                            >
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
