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
import { shortenUuid, UUID_PATTERN } from '@/shared/lib/uuid';
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
            return 'Пользователь с таким UUID не найден';
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
    const [accountId, setAccountId] = useState('');
    const [role, setRole] = useState<SurveyPermissionRole>('ANALYST');
    const [isLoading, setIsLoading] = useState(true);
    const [isCreating, setIsCreating] = useState(false);
    const [pendingAccountId, setPendingAccountId] = useState<string | null>(null);
    const [isValidationVisible, setIsValidationVisible] = useState(false);
    const [copiedAccountId, setCopiedAccountId] = useState<string | null>(null);

    const normalizedAccountId = accountId.trim();
    const isUuidValid = UUID_PATTERN.test(normalizedAccountId);
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

        if (!isUuidValid || isCreating) return;

        setIsCreating(true);
        createSurveyPermission(surveyId, {
            accountId: normalizedAccountId,
            role,
            doNotify: true,
        })
            .then((createdPermission) => {
                setPermissions((currentPermissions) => [...currentPermissions, createdPermission]);
                setAccountId('');
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

        setPendingAccountId(permission.accountId);
        updateSurveyPermissionRole(surveyId, permission.accountId, newRole)
            .then((updatedPermission) => {
                setPermissions((currentPermissions) =>
                    currentPermissions.map((item) =>
                        item.accountId === updatedPermission.accountId ? updatedPermission : item,
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

        setPendingAccountId(permission.accountId);
        deleteSurveyPermission(surveyId, permission.accountId)
            .then(() => {
                setPermissions((currentPermissions) =>
                    currentPermissions.filter((item) => item.accountId !== permission.accountId),
                );
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось удалить права пользователя' }));
            })
            .finally(() => {
                setPendingAccountId(null);
            });
    };

    const copyAccountIdHandler = (value: string) => {
        navigator.clipboard
            .writeText(value)
            .then(() => {
                setCopiedAccountId(value);
                window.setTimeout(() => {
                    setCopiedAccountId((currentValue) => (currentValue === value ? null : currentValue));
                }, 2000);
            })
            .catch(() => {
                dispatch(setErrorMessage({ message: 'Не удалось скопировать UUID пользователя' }));
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
                    value={accountId}
                    onChange={(value) => {
                        setAccountId(value);
                        if (isValidationVisible) setIsValidationVisible(false);
                    }}
                    placeholder='UUID пользователя'
                    errorMessage={
                        isValidationVisible && !isUuidValid ? 'Введите корректный UUID пользователя' : undefined
                    }
                    onBlur={() => {
                        setIsValidationVisible(normalizedAccountId.length > 0 && !isUuidValid);
                    }}
                />
                <Button mode='primary' type='submit' disabled={!isUuidValid || isCreating}>
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
                        <div className={style.permissionRow} key={permission.accountId}>
                            <Select
                                type='label'
                                value={getRoleOption(permission.role)}
                                dataProvider={createStaticDataProvider(ROLE_OPTIONS, 'Роль')}
                                name={`permissionRole-${permission.accountId}`}
                                onChange={(option) =>
                                    updatePermissionHandler(permission, option.value as SurveyPermissionRole)
                                }
                            />
                            <div className={style.accountId}>
                                <span title={permission.accountId}>{shortenUuid(permission.accountId)}</span>
                                <button
                                    className={style.copyButton}
                                    type='button'
                                    title={
                                        copiedAccountId === permission.accountId
                                            ? 'UUID скопирован'
                                            : 'Скопировать полный UUID'
                                    }
                                    aria-label={
                                        copiedAccountId === permission.accountId
                                            ? 'UUID скопирован'
                                            : 'Скопировать полный UUID пользователя'
                                    }
                                    onClick={() => copyAccountIdHandler(permission.accountId)}
                                >
                                    <img className={style.copyIcon} src='/copy.svg' alt='' />
                                </button>
                            </div>
                            <Button
                                mode='secondary'
                                style='negative'
                                type='button'
                                disabled={pendingAccountId === permission.accountId}
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
