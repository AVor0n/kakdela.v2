import { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { ActionList, Button } from '@hh.ru/magritte-ui';
import { routes } from '@/app/routes';
import type { SurveyNavigationItem, SurveySection } from '../../SurveyLayout.types';
import style from './SurveyMobileMenu.module.css';

type Props = {
    surveyId?: string;
    navigationItems: SurveyNavigationItem[];
    activeSection: SurveySection | null;
    canEditSurvey: boolean;
    isAccessLoading: boolean;
    hasSelectedSurvey: boolean;
    isCopied: boolean;
    isPublished?: boolean;
    onPublish: () => void;
    copyClick: () => void;
    isTemplate: boolean;
};

export function SurveyMobileMenu({
    surveyId,
    navigationItems,
    activeSection,
    canEditSurvey,
    isAccessLoading,
    hasSelectedSurvey,
    isPublished,
    onPublish,
    copyClick,
    isCopied,
    isTemplate,
}: Props) {
    const [isOpen, setIsOpen] = useState(false);
    const buttonRef = useRef<HTMLButtonElement>(null!);

    const closeMenu = () => {
        setIsOpen(false);
    };

    const handlePublish = () => {
        onPublish();
        closeMenu();
    };

    return (
        <>
            <Button
                ref={buttonRef}
                mode='secondary'
                style='accent'
                type='button'
                aria-expanded={isOpen}
                aria-controls='survey-mobile-menu'
                aria-haspopup='menu'
                onClick={() => setIsOpen((isMenuOpen) => !isMenuOpen)}
            >
                Меню
            </Button>

            <ActionList
                visible={isOpen}
                onClose={closeMenu}
                dropProps={{
                    activatorRef: buttonRef,
                    placement: 'bottom-left',
                    role: 'menu',
                    maxWidth: 320,
                    padding: 12,
                }}
            >
                <nav id='survey-mobile-menu' className={style.menuContent} aria-label='Меню опроса'>
                    <Button mode='tertiary' style='accent' Element={Link} to={routes.survey()} onClick={closeMenu}>
                        Обратно в меню
                    </Button>

                    {!isTemplate &&
                        navigationItems.map((item) => (
                            <Button
                                key={item.section}
                                mode={activeSection === item.section ? 'primary' : 'tertiary'}
                                style='accent'
                                Element={Link}
                                to={item.path}
                                disabled={item.disabled}
                                title={item.disabled ? item.disabledTitle : undefined}
                                aria-label={item.disabled ? item.disabledAriaLabel : item.label}
                                onClick={closeMenu}
                            >
                                {item.label}
                            </Button>
                        ))}

                    <Button mode='tertiary' style={isCopied ? 'positive' : 'neutral'} onClick={copyClick}>
                        {isCopied ? 'Ссылка скопирована' : 'Скопировать ссылку на опрос'}
                    </Button>

                    {surveyId && (
                        <Button
                            mode='tertiary'
                            style='neutral'
                            Element={Link}
                            to={routes.surveyPreview(surveyId)}
                            disabled={!hasSelectedSurvey || isAccessLoading}
                            onClick={closeMenu}
                        >
                            Предпросмотр
                        </Button>
                    )}

                    {canEditSurvey && (
                        <Button
                            mode={isPublished ? 'secondary' : 'tertiary'}
                            style={isPublished ? 'positive' : 'accent'}
                            onClick={handlePublish}
                            disabled={!hasSelectedSurvey}
                        >
                            {isPublished ? 'Снять с публикации' : 'Опубликовать'}
                        </Button>
                    )}
                </nav>
            </ActionList>
        </>
    );
}
