import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import App from '@/pages/Root/App';
import { routePatterns, routes } from '@/app/routes';
import { Login } from '@/pages/Auth/Login/Login';
import { Register } from '@/pages/Auth/Register/Register';
import { SurveyList } from '@/pages/Survey/components/SurveyList/SurveyList';
import { SurveyView } from '@/pages/Survey/SurveyView/SurveyView';
import { SurveyModify } from '@/pages/Survey/SurveyModify/SurveyModify';
import { SurveyCreate } from '@/pages/Survey/SurveyCreate/SurveyCreate';
import { SurveyLayout } from '@/layouts/SurveyLayout/SurveyLayout';
import { RequireAuth } from '@/features/auth/RequireAuth';
import { Settings } from '@/pages/Survey/SurveyModify/components/Settings/Settings';
import { NotFound } from '@/pages/Errors/NotFound';
import { Answers } from '@/pages/Survey/SurveyModify/components/Answers/Answers';
import { AccountBootstrap } from '@/features/auth/AccountBootstrap';
import { ForgotPassword } from '@/pages/Auth/ForgotPassword/ForgotPassword';
import { VerifyCode } from '@/pages/Auth/VerifyCode/VerifyCode';
import { ResetPassword } from '@/pages/Auth/ResetPassword/ResetPassword';

export function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path={routePatterns.root} element={<App />} />
                <Route path={routePatterns.forgotPassword} element={<ForgotPassword />} />
                <Route path={routePatterns.resetPassword} element={<ResetPassword />} />
                <Route path={routePatterns.verifyCode} element={<VerifyCode />} />
                <Route path={routePatterns.authCallback} element={<Navigate to={routes.root()} />} />
                <Route path={routePatterns.auth}>
                    <Route path={routePatterns.authLogin} element={<Login />} />
                    <Route path={routePatterns.authRegister} element={<Register />} />
                </Route>

                <Route path={routePatterns.surveys}>
                    <Route
                        path={routePatterns.surveys}
                        element={
                            <AccountBootstrap>
                                <RequireAuth>
                                    <SurveyList />
                                </RequireAuth>
                            </AccountBootstrap>
                        }
                    />
                    <Route>
                        <Route path={routePatterns.surveysView} element={<SurveyView />} />
                        <Route element={<SurveyLayout />}>
                            <Route
                                path={routePatterns.surveyModify}
                                element={
                                    <AccountBootstrap>
                                        <RequireAuth>
                                            <Outlet />
                                        </RequireAuth>
                                    </AccountBootstrap>
                                }
                            >
                                <Route path='settings' element={<Settings />} />
                                <Route path='questions' element={<SurveyModify />} />
                                <Route path='answers' element={<Answers />} />
                            </Route>
                            <Route
                                path={routePatterns.surveyCreate}
                                element={
                                    <AccountBootstrap>
                                        <RequireAuth>
                                            <SurveyCreate />
                                        </RequireAuth>
                                    </AccountBootstrap>
                                }
                            >
                                <Route path='settings' element={<Settings />} />
                                <Route path='questions' element={<div>Questions</div>} />
                                <Route path='answers' element={<div>Answers</div>} />
                            </Route>
                        </Route>
                    </Route>
                </Route>
                <Route path='*' element={<NotFound />} />
            </Routes>
        </BrowserRouter>
    );
}
