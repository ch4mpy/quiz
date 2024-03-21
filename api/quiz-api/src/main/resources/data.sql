CREATE TABLE public.databasechangelog (
	id varchar(255) NOT NULL,
	author varchar(255) NOT NULL,
	filename varchar(255) NOT NULL,
	dateexecuted timestamp NOT NULL,
	orderexecuted int4 NOT NULL,
	exectype varchar(10) NOT NULL,
	md5sum varchar(35) NULL,
	description varchar(255) NULL,
	"comments" varchar(255) NULL,
	tag varchar(255) NULL,
	liquibase varchar(20) NULL,
	contexts varchar(255) NULL,
	labels varchar(255) NULL,
	deployment_id varchar(10) NULL
);
CREATE TABLE public.databasechangeloglock (
	id int4 NOT NULL,
	"locked" bool NOT NULL,
	lockgranted timestamp NULL,
	lockedby varchar(255) NULL,
	CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id)
);
INSERT INTO public.databasechangeloglock
(id, "locked", lockgranted, lockedby)
VALUES(0, false, null, null);
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-1', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.727', 1, 'EXECUTED', '9:804d3d97ac04214e8172b06e4ebbacea', 'createTable tableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-2', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.776', 2, 'EXECUTED', '9:752b1ab0429649cce094a9546d687f8c', 'createTable tableName=quiz_skill_tests', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-3', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.778', 3, 'EXECUTED', '9:d36cf434feacb5dae7ec681e6497c4a9', 'addUniqueConstraint constraintName=uk_c3giyfjx14o0r040dus4fxr01, tableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-4', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.780', 4, 'EXECUTED', '9:24cc0e1fb0d8b24c9baeae7fe3e2e654', 'addUniqueConstraint constraintName=uk_fvl8i29newwhr9jvd8ikxhk0i, tableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-5', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.782', 5, 'EXECUTED', '9:6e0ff023e9b8d2d909ae8eaf4b4b9e78', 'addUniqueConstraint constraintName=uk_lq3b8pvf33jm4fq2cr77kdah1, tableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-6', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.783', 6, 'EXECUTED', '9:425c2f28894562b668859a1544f29a02', 'addUniqueConstraint constraintName=uk_rm4c0bev4b69qfena0jwrmk8y, tableName=quiz_skill_tests', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-7', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.785', 7, 'EXECUTED', '9:ad05600e37817ba290d6c5d65e2dc658', 'createSequence sequenceName=choice_seq', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-8', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.786', 8, 'EXECUTED', '9:5b74b6aa0b07c69c3f24bb91c0955e3c', 'createSequence sequenceName=question_seq', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-9', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.788', 9, 'EXECUTED', '9:570c2c11e43fd896f3321adef88deb54', 'createSequence sequenceName=quiz_seq', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-10', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.789', 10, 'EXECUTED', '9:e335de9845a8b7541c0add30f08ba7b8', 'createTable tableName=choice', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-11', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.792', 11, 'EXECUTED', '9:0248b13496baf46b6d8e212757f07758', 'createTable tableName=question', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-12', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.793', 12, 'EXECUTED', '9:f903ea3972a208d4b2e32faa5817a286', 'createTable tableName=skill_test', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-13', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.795', 13, 'EXECUTED', '9:9f01f290d6b124e0bdb9d7d2726f5f78', 'createTable tableName=skill_test_choices', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-14', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.796', 14, 'EXECUTED', '9:74ef71b09b70c24595fe59e5c377c29a', 'addForeignKeyConstraint baseTableName=quiz, constraintName=fk1a9ofmpkpxe0grsno9lsvniq3, referencedTableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-15', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.797', 15, 'EXECUTED', '9:43b8c908b236f6aebe207e3d38409985', 'addForeignKeyConstraint baseTableName=skill_test_choices, constraintName=fk57lqfp8gi1suuch5h65r0wuxo, referencedTableName=choice', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-16', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.799', 16, 'EXECUTED', '9:2d51b993fa8deda7b27a75f42b4be575', 'addForeignKeyConstraint baseTableName=quiz, constraintName=fk7qshmcdwkofdd4auc7bt4k6fd, referencedTableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-17', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.800', 17, 'EXECUTED', '9:4ab00285805285284c06b51ccae6fd17', 'addForeignKeyConstraint baseTableName=question, constraintName=fkb0yh0c1qaxfwlcnwo9dms2txf, referencedTableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-18', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.801', 18, 'EXECUTED', '9:47ec92f4ed28719c5b003c5a1984706b', 'addForeignKeyConstraint baseTableName=choice, constraintName=fkcaq6r76cswke5b9fk6fyx3y5w, referencedTableName=question', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-19', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.803', 19, 'EXECUTED', '9:43494e6f0ee9feef124956c2b135232e', 'addForeignKeyConstraint baseTableName=skill_test_choices, constraintName=fkdcgd4aib37486v4pv541drdww, referencedTableName=skill_test', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-20', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.804', 20, 'EXECUTED', '9:67d41b8c5a1e97ec3a19a2dd6867fee7', 'addForeignKeyConstraint baseTableName=quiz_skill_tests, constraintName=fkiyg4mp0vi47kbdx6jx3cw5ow1, referencedTableName=skill_test', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-21', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.805', 21, 'EXECUTED', '9:abbc5b29650467952224397e55c1acd6', 'addForeignKeyConstraint baseTableName=quiz, constraintName=fklxisdmbxbv669bfjbdkp8xush, referencedTableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');
INSERT INTO public.databasechangelog
(id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, "comments", tag, liquibase, contexts, labels, deployment_id)
VALUES('init-22', 'ch4mpy', 'db/changelog/db-changelog-master.xml', '2024-03-21 07:32:05.807', 22, 'EXECUTED', '9:4dcfda60047ad1945b6a8fed278c0453', 'addForeignKeyConstraint baseTableName=quiz_skill_tests, constraintName=fktfhr5qj312utua4qar03uyiw7, referencedTableName=quiz', '', NULL, '4.26.0', 'init', NULL, '1042325763');