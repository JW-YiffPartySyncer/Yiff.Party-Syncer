-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Erstellungszeit: 11. Jul 2020 um 15:07
-- Server-Version: 10.4.11-MariaDB
-- PHP-Version: 7.4.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `yiffparty`
--
CREATE DATABASE IF NOT EXISTS `yiffparty` DEFAULT CHARACTER SET utf32 COLLATE utf32_general_ci;
USE `yiffparty`;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `patreons`
--

CREATE TABLE `patreons` (
  `ID` int(11) NOT NULL,
  `link` text NOT NULL,
  `name` text NOT NULL DEFAULT '',
  `last_checked` bigint(20) NOT NULL DEFAULT 0,
  `success` tinyint(1) NOT NULL DEFAULT 0,
  `wanted` tinyint(4) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf32;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `posts`
--

CREATE TABLE `posts` (
  `ID` int(11) NOT NULL,
  `patreon` int(11) NOT NULL,
  `name` text NOT NULL,
  `href` text NOT NULL,
  `downloaded` tinyint(1) NOT NULL DEFAULT 0,
  `post` tinytext NOT NULL,
  `date` tinytext NOT NULL DEFAULT '',
  `last_checked` bigint(20) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf32;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `webrip`
--

CREATE TABLE `webrip` (
  `ID` int(11) NOT NULL,
  `Timestamp` int(11) NOT NULL,
  `Part` smallint(6) NOT NULL,
  `data` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf32;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `patreons`
--
ALTER TABLE `patreons`
  ADD PRIMARY KEY (`ID`);

--
-- Indizes für die Tabelle `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `HASH` (`href`(768)) USING HASH,
  ADD KEY `downloaded_ID_lastChecked` (`downloaded`,`ID`,`last_checked`);

--
-- Indizes für die Tabelle `webrip`
--
ALTER TABLE `webrip`
  ADD PRIMARY KEY (`ID`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `patreons`
--
ALTER TABLE `patreons`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `posts`
--
ALTER TABLE `posts`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `webrip`
--
ALTER TABLE `webrip`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
