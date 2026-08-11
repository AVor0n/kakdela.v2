import {
    createSurveyPermission,
    deleteSurveyPermission,
    getSurveyPermissions,
    updateSurveyPermissionRole,
    type SurveyPermission,
    type SurveyPermissionRole,
} from '@/api/permissions';
import { setErrorMessage } from '@/entities/Error/Error.slice';
import { useAppDispatch } from '@/hooks/useAppDispatch';
import { validateEmail } from '@/pages/Auth/validation';
import {
    Button,
    createStaticDataProvider,
    FormLabel,
    Input,
    Loader,
    Select,
    type StaticDataFetcherItem,
} from '@hh.ru/magritte-ui';
import axios from 'axios';
import { useEffect, useMemo, useState, type FormEvent } from 'react';
import style from './Permissions.module.css';

type Props = {
    surveyId: string;
};

const ROLE_OPTIONS: StaticDataFetcherItem[] = [
    { value: 'ANALYST', text: 'Аналитик' },
    { value: 'EDITOR', text: 'Редактор' },
];

function getRoleOption(role: SurveyPermissionRole): StaticDataFetcherItem | undefined {
    return ROLE_OPTIONS.find((option) => option.value === role);
}

function getCreateErrorMessage(error: unknown): string {
    if (!axios.isAxiosError(error)) return 'Не удалось наделить пользователя правами';

    switch (error.response?.status) {
        case 404:
            return 'Пользователь с такой почтой не найден';
        case 409:
            return 'Этот пользователь уже имеет права в опросе';
        case 403:
            return 'Только автор опроса может управлять правами';
        default:
            return 'Не удалось наделить пользователя правами';
    }
}

export function Permissions({ surveyId }: Props) {
    const dispatch = useAppDispatch();
    const [permissions, setPermissions] = useState<SurveyPermission[]>([]);
    const [email, setEmail] = useState('');
    const [role, setRole] = useState<SurveyPermissionRole>('ANALYST');
    const [isLoading, setIsLoading] = useState(true);
    const [isCreating, setIsCreating] = useState(false);
    const [pendingAccountId, setPendingAccountId] = useState<string | null>(null);
    const [isValidationVisible, setIsValidationVisible] = useState(false);

    const normalizedEmail = email.trim();
    const emailError = validateEmail(normalizedEmail);
    const isEmailValid = emailError === '';
    const selectedRole = useMemo(() => getRoleOption(role), [role]);

    useEffect(() => {
        let isActive = true;

        setIsLoading(true);
        getSurveyPermissions(surveyId)
            .then((data) => {
                if (isActive) setPermissions(data);
            })
            .catch(() => {
                if (isActive) {
                    dispatch(setErrorMessage({ message: 'Не удалось получить права пользователей этого опроса' }));
                }
            })
            .finally(() => {
                if (isActive) setIsLoading(false);
            });

        return () => {
            isActive = false;
        };
    }, [dispatch, surveyId]);

    const createPermissionHandler = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setIsValidationVisible(true);

        if (!isEmailValid || isCreating) return;

        setIsCreating(true);
        createSurveyPermission(surveyId, {
            email: normalizedEmail,
            role,
        })
            .then((createdPermission) => {
                setPermissions((currentPermissions) => [...currentPermissions, createdPermission]);
                setEmail('');
                setIsValidationVisible(false);
            })
            .catch((error: unknown) => {
                dispatch(setErrorMessage({ message: getCreateErrorMessage(error) }));
            })
            .finally(() => {
                setIsCreating(false);
            });
    };

    const updatePermissionHandler = (permission: SurveyPermission, newRole: SurveyPermissionRole) => {
        if (newRole === permission.role || pendingAccountId) return;

        setPendingAccountId(permission.account.id);
        updateSurveyPermissionRole(surveyId, permission.account.id, newRole)
            .then((updatedPermission) => {
                setPermissions((currentPermissions) =>
                    currentPermissions.map((item) =>
                        item.account.id === updatedPermission.account.id ? updatedPermission : item,
                    ),
                );
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось изменить роль пользователя' }));
            })
            .finally(() => {
                setPendingAccountId(null);
            });
    };

    const deletePermissionHandler = (permission: SurveyPermission) => {
        if (pendingAccountId) return;

        setPendingAccountId(permission.account.id);
        deleteSurveyPermission(surveyId, permission.account.id)
            .then(() => {
                setPermissions((currentPermissions) =>
                    currentPermissions.filter((item) => item.account.id !== permission.account.id),
                );
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось удалить права пользователя' }));
            })
            .finally(() => {
                setPendingAccountId(null);
            });
    };

    return (
        <section className={style.content}>
            <FormLabel>Наделение правами</FormLabel>
            <form className={style.formRow} onSubmit={createPermissionHandler}>
                <Select
                    type='label'
                    value={selectedRole}
                    dataProvider={createStaticDataProvider(ROLE_OPTIONS, 'Роль')}
                    name='permissionRole'
                    onChange={(option) => setRole(option.value as SurveyPermissionRole)}
                />
                <Input
                    value={email}
                    onChange={(value) => {
                        setEmail(value);
                        if (isValidationVisible) setIsValidationVisible(false);
                    }}
                    placeholder='Email пользователя'
                    type='email'
                    invalid={isValidationVisible && !isEmailValid}
                    errorMessage={isValidationVisible ? emailError : undefined}
                    onBlur={() => {
                        setIsValidationVisible(!isEmailValid);
                    }}
                />
                <Button mode='primary' type='submit' disabled={!isEmailValid || isCreating}>
                    {isCreating ? 'Добавление…' : 'Добавить'}
                </Button>
            </form>

            {isLoading ? (
                <div className={style.loader}>
                    <Loader size={24} />
                </div>
            ) : permissions.length > 0 ? (
                <div className={style.list}>
                    {permissions.map((permission) => (
                        <div className={style.permissionRow} key={permission.account.id}>
                            <Select
                                type='label'
                                value={getRoleOption(permission.role)}
                                dataProvider={createStaticDataProvider(ROLE_OPTIONS, 'Роль')}
                                name={`permissionRole-${permission.account.id}`}
                                onChange={(option) =>
                                    updatePermissionHandler(permission, option.value as SurveyPermissionRole)
                                }
                            />
                            <div className={style.accountDetails}>
                                <span className={style.email}>{permission.account.email}</span>
                            </div>
                            <Button
                                mode='secondary'
                                style='negative'
                                type='button'
                                disabled={pendingAccountId === permission.account.id}
                                icon={<img src='/trash.svg' alt='Удалить права' />}
                                onClick={() => deletePermissionHandler(permission)}
                            />
                        </div>
                    ))}
                </div>
            ) : (
                <p className={style.empty}>Пользователи с правами ещё не добавлены</p>
            )}
        </section>
    );
}
