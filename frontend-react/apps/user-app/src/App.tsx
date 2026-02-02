import { useState } from 'react'
import '@atlaskit/css-reset';
import {
  PageLayout,
  Content,
  LeftSidebar,
  Main,
} from '@atlaskit/page-layout';
import { SideNavigation, Section, ButtonItem, Header } from '@atlaskit/side-navigation';
import Textfield from '@atlaskit/textfield';
import SearchIcon from '@atlaskit/icon/core/search';
import FolderIcon from '@atlaskit/icon/core/folder-closed';
import ImageOrIcon from '@atlaskit/icon/core/image';
import CalendarIcon from '@atlaskit/icon/core/calendar';
import AppSwitcherIcon from '@atlaskit/icon/core/app-switcher';

function App() {
  const [searchTerm, setSearchTerm] = useState('');

  return (
    <PageLayout>
      <Content>
        <LeftSidebar>
          <SideNavigation label="Search filters">
            <Section>
              <Header>Filters</Header>
              <ButtonItem iconBefore={<FolderIcon label="Space" />}>Space</ButtonItem>
              <ButtonItem iconBefore={<ImageOrIcon label="Type" />}>Type</ButtonItem>
              <ButtonItem iconBefore={<CalendarIcon label="Date" />}>Date</ButtonItem>
              <ButtonItem iconBefore={<AppSwitcherIcon label="Space category" />}>Space category</ButtonItem>
            </Section>
            <Section>
              <ButtonItem><span style={{ color: '#0052CC' }}>Advanced search</span></ButtonItem>
            </Section>
          </SideNavigation>
        </LeftSidebar>
        <Main>
          <div style={{ height: '100vh', padding: '2rem' }}>
            {/* Search Bar */}
            <div style={{ marginBottom: '2rem' }}>
              <Textfield
                placeholder="Search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.currentTarget.value)}
                elemAfterInput={
                  <div style={{ marginRight: '8px', display: 'flex', alignItems: 'center' }}>
                    <SearchIcon label="search" />
                  </div>
                }
                appearance="standard"
              />
              <div style={{ marginTop: '2rem', display: 'flex', flexDirection: 'column', gap: '2rem', height: '100%', overflowY: 'auto' }}>
                {/* Result Item Mock */}
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ width: 32, height: 32, borderRadius: 4, backgroundColor: '#0052CC', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}>
                    A
                  </div>
                  <div>
                    <a href="#" style={{ fontSize: '1.125rem', fontWeight: 500, color: '#0052CC', textDecoration: 'none' }}>Example Result Title</a>
                    <div style={{ fontSize: '0.75rem', color: '#6B778C', marginTop: '0.125rem' }}>
                      Space Name • Updated Dec 14, 2025
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#172B4D', marginTop: '0.5rem', lineHeight: '1.5' }}>
                      This is a snippet of the search result content. It shows a preview of what's inside the page...
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Main>
      </Content>
    </PageLayout>
  )
}

export default App
